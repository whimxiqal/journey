/*
 * MIT License
 *
 * Copyright 2021 Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package edu.whimc.journey.common.data.sql;

import edu.whimc.journey.common.JourneyCommon;
import edu.whimc.journey.common.data.DataAccessException;
import edu.whimc.journey.common.data.PathReportManager;
import edu.whimc.journey.common.navigation.Cell;
import edu.whimc.journey.common.navigation.Path;
import edu.whimc.journey.common.navigation.Step;
import edu.whimc.journey.common.search.FlexiblePathTrial;
import edu.whimc.journey.common.search.PathTrial;
import edu.whimc.journey.common.util.Extra;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class SqlPathReportManager<T extends Cell<T, D>, D>
    extends SqlManager<T, D>
    implements PathReportManager<T, D> {

  private static final String PATH_REPORT_TABLE_NAME = "path_report";
  private static final String PATH_REPORT_CELL_TABLE_NAME = "path_report_cell";
  private long lastReturnedId = -1;
  private Lock retrievalLock = new ReentrantLock();

  public SqlPathReportManager(SqlConnectionController connectionController, DataAdapter<T, D> dataAdapter) {
    super(connectionController, dataAdapter);
    createTables();
  }

  @Override
  public void addReport(PathTrial<T, D> trial,
                        Collection<FlexiblePathTrial.Node<T, D>> calculationNodes,
                        long executionTime)
      throws DataAccessException {
    Path<T, D> path = trial.getPath();
    if (path == null) {
      throw new IllegalArgumentException("The path of he input path trial was not valid."
          + " The input path trial must be successful and have a valid path.");
    }

    long pathReportId = -1;
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
              "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
              PATH_REPORT_TABLE_NAME,
              "timestamp",
              "duration",
              "origin_x",
              "origin_y",
              "origin_z",
              "destination_x",
              "destination_y",
              "destination_z",
              "world_uuid"),
          Statement.RETURN_GENERATED_KEYS);

      statement.setLong(1, System.currentTimeMillis() / 1000);
      statement.setInt(2, (int) executionTime);
      statement.setInt(3, trial.getOrigin().getX());
      statement.setInt(4, trial.getOrigin().getY());
      statement.setInt(5, trial.getOrigin().getZ());
      statement.setInt(6, trial.getDestination().getX());
      statement.setInt(7, trial.getDestination().getY());
      statement.setInt(8, trial.getDestination().getZ());
      statement.setString(9, getDataAdapter().getDomainIdentifier(trial.getDomain())
          .replaceAll("-", ""));

      statement.execute();

      try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          pathReportId = generatedKeys.getLong(1);
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }

    if (pathReportId < 0) {
      throw new DataAccessException("No id found from the inserted path report");
    }

    // Let's calculate the deviation of every node in this trial
    final double totalDistance = trial.getOrigin().distanceTo(path.getDestination());
    final double deviationStepRatio = totalDistance / trial.getLength();
    final Map<T, Double> deviations = new HashMap<>();

    // Get all the deviations for everything on the critical path
    // Subtract deviation step to start because the first step is one step forward
    double currentDeviation = totalDistance;
    for (Step<T, D> step : path.getSteps()) {
      currentDeviation -= deviationStepRatio * step.length();
      if (currentDeviation < 0.5) {
        currentDeviation = 0; // floor to 0 because we're basically at the end... no need for decimals anymore
      }
      deviations.put(step.location(), currentDeviation);
    }

    // Get all the rest of the deviations
    for (FlexiblePathTrial.Node<T, D> node : calculationNodes) {
      if (deviations.containsKey(node.getData().location())) {
        // We already know this deviation so continue
        continue;
      }

      // We don't know the deviation of this one, so check backwards until we find one that we know,
      // then work forwards again to add the deviations, which will keep increasing.
      // Basically, we will be working our way towards the heart of the tree (the destination),
      // then go back outwards to the leaves once we know how far away some point along the way
      // is from the heart.
      Stack<FlexiblePathTrial.Node<T, D>> stack = new Stack<>();
      FlexiblePathTrial.Node<T, D> current = node;
      while (!deviations.containsKey(current.getData().location())) {
        stack.add(current);
        current = current.getPrevious();
      }
      // We know what current's deviation is. Work forward now
      while (!stack.isEmpty()) {
        deviations.put(stack.peek().getData().location(),
            deviations.get(current.getData().location()) + stack.peek().getData().length());
        stack.pop();
      }
    }
    // We are done setting up the deviations
    assert deviations.size() == calculationNodes.size();

    for (FlexiblePathTrial.Node<T, D> node : calculationNodes) {
      try (Connection connection = getConnectionController().establishConnection()) {
        PreparedStatement statement = connection.prepareStatement(String.format(
            "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
            PATH_REPORT_CELL_TABLE_NAME,
            "path_report_id",
            "x",
            "y",
            "z",
            "deviation",
            "distance",
            "distance_y",
            "biome",
            "dimension"));

        statement.setLong(1, pathReportId);
        statement.setLong(2, node.getData().location().getX());
        statement.setLong(3, node.getData().location().getY());
        statement.setLong(4, node.getData().location().getZ());
        statement.setDouble(5, deviations.get(node.getData().location()));
        statement.setDouble(6, node.getData().location().distanceTo(path.getDestination()));
        statement.setInt(7, Math.abs(node.getData().location().getY() - path.getDestination().getY()));
        statement.setInt(8, JourneyCommon.<T, D>getConversions()
            .getBiome(node.getData().location()));
        statement.setInt(9, JourneyCommon.<T, D>getConversions()
            .getDimension(node.getData().location().getDomain()));

        statement.execute();
      } catch (SQLException e) {
        e.printStackTrace();
        throw new DataAccessException();
      }
    }

  }

  @Override
  public PathTrialRecord getNextReport() {
    retrievalLock.lock();
    try (Connection connection = getConnectionController().establishConnection()) {
      ResultSet resultSet = connection.prepareStatement("SELECT * FROM "
          + PATH_REPORT_TABLE_NAME
          + " WHERE id > "
          + lastReturnedId
          + " ORDER BY id"
          + " LIMIT 1;").executeQuery();

      PathTrialRecord record;
      if (resultSet.next()) {
        long id = resultSet.getLong(1);
        record = new PathTrialRecord(id,
            new Date(resultSet.getLong(2)),
            resultSet.getInt(4),
            resultSet.getInt(5),
            resultSet.getInt(6),
            resultSet.getInt(7),
            resultSet.getInt(8),
            resultSet.getInt(9),
            Extra.fromPlainString(resultSet.getString(10)),
            new LinkedList<>());

        if (resultSet.next()) {
          // Not so much of a problem, but I just want to make sure we're consistent
          throw new DataAccessException("Result set returned too many values!");
        }

        ResultSet cells = connection.prepareStatement("SELECT * FROM "
            + PATH_REPORT_CELL_TABLE_NAME
            + " WHERE path_report_id = "
            + id
            + ";").executeQuery();

        while (cells.next()) {
          record.cells().add(new PathTrialCellRecord(
              record,
              cells.getInt(2),
              cells.getInt(3),
              cells.getInt(4),
              cells.getInt(5),
              cells.getInt(6),
              cells.getInt(7),
              cells.getInt(8),
              cells.getInt(9)));
        }
        lastReturnedId = record.id();
        retrievalLock.unlock();
        return record;

      } else {
        lastReturnedId = -1;
        retrievalLock.unlock();
        return null;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      retrievalLock.unlock();
      return null;
    }
  }

  protected void createTables() {
    try (Connection connection = getConnectionController().establishConnection()) {
      String pathTableStatement = "CREATE TABLE IF NOT EXISTS "
          + PATH_REPORT_TABLE_NAME + " ("
          + "id integer PRIMARY KEY AUTOINCREMENT, "
          + "timestamp integer NOT NULL, "
          + "duration integer NOT NULL, "
          + "origin_x int(7) NOT NULL,"
          + "origin_y int(7) NOT NULL,"
          + "origin_z int(7) NOT NULL,"
          + "destination_x int(7) NOT NULL,"
          + "destination_y int(7) NOT NULL,"
          + "destination_z int(7) NOT NULL,"
          + "world_uuid binary(16) NOT NULL"
          + ");";
      connection.prepareStatement(pathTableStatement).execute();

      String cellTableStatement = "CREATE TABLE IF NOT EXISTS "
          + PATH_REPORT_CELL_TABLE_NAME + " ("
          + "path_report_id integer NOT NULL, "
          + "x int(7) NOT NULL,"
          + "y int(7) NOT NULL,"
          + "z int(7) NOT NULL,"
          + "deviation double(12, 5) NOT NULL,"
          + "distance double(12, 5) NOT NULL,"
          + "distance_y int(3) NOT NULL,"
          + "biome int NOT NULL, "
          + "dimension int NOT NULL, "
          + "FOREIGN KEY (path_report_id) REFERENCES " + PATH_REPORT_TABLE_NAME + "(id)"
          + " ON DELETE CASCADE"
          + " ON UPDATE CASCADE"
          + ");";
      connection.prepareStatement(cellTableStatement).execute();

      String indexStatement = "CREATE INDEX IF NOT EXISTS path_report_idx ON "
          + PATH_REPORT_CELL_TABLE_NAME
          + " (path_report_id);";
      connection.prepareStatement(indexStatement).execute();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
