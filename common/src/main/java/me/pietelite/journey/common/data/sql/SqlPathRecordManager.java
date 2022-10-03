/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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
 */

package me.pietelite.journey.common.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import me.pietelite.journey.common.data.DataAccessException;
import me.pietelite.journey.common.data.PathRecordManager;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.ModeType;
import me.pietelite.journey.common.navigation.Path;
import me.pietelite.journey.common.navigation.Step;
import me.pietelite.journey.common.search.PathTrial;
import me.pietelite.journey.common.search.ScoringFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A generic path record manager for SQL storage.
 */
public abstract class SqlPathRecordManager
    extends SqlManager
    implements PathRecordManager {

  private static final String PATH_RECORD_TABLE_NAME = "path_record";
  private static final String PATH_RECORD_CELL_TABLE_NAME = "path_record_cell";
  private static final String PATH_RECORD_MODE_TABLE_NAME = "path_record_mode";

  /**
   * General constructor.
   *
   * @param connectionController the connection controller
   */
  public SqlPathRecordManager(SqlConnectionController connectionController) {
    super(connectionController);
    createTables();
  }

  @Override
  public void report(PathTrial trial,
                     Set<ModeType> modeTypes,
                     long executionTime)
      throws DataAccessException {
    Path path = trial.getPath();
    if (path == null) {
      throw new IllegalArgumentException("The path of he input path trial was not valid."
          + " The input path trial must be successful and have a valid path.");
    }
    if (path.getSteps().isEmpty()) {
      // This is not something we need to report
      return;
    }

    /* TODO Add back this deletion mechanism
    // Delete previous record if it has the same origin/destination/world and is slower
    ModeTypeGroup modeTypes = ModeTypeGroup.from(trial.getModes());
    if (containsRecord(trial.getOrigin(), trial.getDestination(), modeTypes)) {
      // Let's delete the last one, so we can store the new one
      try (Connection connection = getConnectionController().establishConnection()) {
        connection.prepareStatement("DELETE FROM " + PATH_RECORD_TABLE_NAME
                + " WHERE "
                + "origin_x = " + trial.getOrigin().getX() + " AND "
                + "origin_y = " + trial.getOrigin().getY() + " AND "
                + "origin_z = " + trial.getOrigin().getZ() + " AND "
                + "destination_x = " + trial.getDestination().getX() + " AND "
                + "destination_y = " + trial.getDestination().getY() + " AND "
                + "destination_z = " + trial.getDestination().getZ() + " AND "
                + "world_uuid = '" + trial.getOrigin().getDomainId() + "'")
            .execute();
      } catch (SQLException e) {
        e.printStackTrace();
        throw new DataAccessException();
      }
    }
     */

    long pathReportId = -1;
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
              "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) "
                  + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
              PATH_RECORD_TABLE_NAME,
              "timestamp",
              "duration",
              "path_length",
              "origin_x",
              "origin_y",
              "origin_z",
              "destination_x",
              "destination_y",
              "destination_z",
              "world_uuid",
              "scoring_function"),
          Statement.RETURN_GENERATED_KEYS);

      statement.setLong(1, System.currentTimeMillis() / 1000);
      statement.setInt(2, (int) executionTime);
      statement.setDouble(3, trial.getLength());
      statement.setInt(4, trial.getOrigin().getX());
      statement.setInt(5, trial.getOrigin().getY());
      statement.setInt(6, trial.getOrigin().getZ());
      statement.setInt(7, trial.getDestination().getX());
      statement.setInt(8, trial.getDestination().getY());
      statement.setInt(9, trial.getDestination().getZ());
      statement.setString(10, trial.getDomain());
      statement.setString(11, trial.getScoringFunction().getType().name());

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
      throw new DataAccessException("No id found from the inserted path record");
    }
    // Get all the rest of the deviations
    ArrayList<Step> steps = path.getSteps();
    for (int i = 0; i < steps.size(); i++) {
      Step step = steps.get(i);
      try (Connection connection = getConnectionController().establishConnection()) {
        PreparedStatement statement = connection.prepareStatement(String.format(
            "INSERT INTO %s (%s, %s, %s, %s, %s, %s) "
                + "VALUES (?, ?, ?, ?, ?, ?);",
            PATH_RECORD_CELL_TABLE_NAME,
            "path_record_id",
            "x", "y", "z",
            "path_index",
            "mode_type"));

        statement.setLong(1, pathReportId);
        statement.setLong(2, step.location().getX());
        statement.setLong(3, step.location().getY());
        statement.setLong(4, step.location().getZ());
        statement.setLong(6, i);
        statement.setInt(7, step.getModeType().ordinal());
        statement.execute();
      } catch (SQLException e) {
        e.printStackTrace();
        throw new DataAccessException();
      }
    }

    for (ModeType modeType : modeTypes) {
      try (Connection connection = getConnectionController().establishConnection()) {
        PreparedStatement statement = connection.prepareStatement(String.format(
            "INSERT INTO %s (%s, %s) VALUES (?, ?);",
            PATH_RECORD_MODE_TABLE_NAME,
            "path_record_id",
            "mode_type"));

        statement.setLong(1, pathReportId);
        statement.setInt(2, modeType.ordinal());

        statement.execute();
      } catch (SQLException e) {
        e.printStackTrace();
        throw new DataAccessException();
      }
    }

  }

  @Override
  public void clear() {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE * FROM %s;",
          PATH_RECORD_MODE_TABLE_NAME));
      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  /**
   * Get all records that have this origin and destination,
   * but do not populate the internal cells, but <b>do</b> populate
   * the modes.
   *
   * @param origin      the origin location
   * @param destination the destination location
   * @return a list of all records
   */
  private List<PathTrialRecord> getRecordsWithoutCells(Cell origin, Cell destination) {
    try (Connection connection = getConnectionController().establishConnection()) {
      ResultSet recordResult = connection.prepareStatement("SELECT * FROM "
              + PATH_RECORD_TABLE_NAME
              + " WHERE "
              + "origin_x = " + origin.getX() + " AND "
              + "origin_y = " + origin.getY() + " AND "
              + "origin_z = " + origin.getZ() + " AND "
              + "destination_x = " + destination.getX() + " AND "
              + "destination_y = " + destination.getY() + " AND "
              + "destination_z = " + destination.getZ() + " AND "
              + "world_uuid = '" + origin.domainId() + "'")
          .executeQuery();
      List<PathTrialRecord> records = new LinkedList<>();
      while (recordResult.next()) {
        PathTrialRecord record = extractRecord(recordResult);
        ResultSet modeResult = connection.prepareStatement("SELECT * FROM "
            + PATH_RECORD_MODE_TABLE_NAME
            + " WHERE "
            + "path_record_id = " + record.id()).executeQuery();
        while (modeResult.next()) {
          record.modes().add(new PathTrialModeRecord(record,
              ModeType.values()[(modeResult.getInt("mode_type"))]));
        }
        records.add(record);
      }

      return records;
    } catch (SQLException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  @Override
  public @NotNull List<PathTrialRecord> getRecords(Cell origin, Cell destination) {
    try (Connection connection = getConnectionController().establishConnection()) {
      List<PathTrialRecord> emptyRecords = getRecordsWithoutCells(origin, destination);

      // Add the subcomponents (modes and cells) to the previously empty records
      for (PathTrialRecord emptyRecord : emptyRecords) {
        ResultSet cellResult = connection.prepareStatement("SELECT * FROM "
            + PATH_RECORD_CELL_TABLE_NAME
            + " WHERE "
            + "path_record_id = " + emptyRecord.id()).executeQuery();
        while (cellResult.next()) {
          emptyRecord.cells().add(extractCell(emptyRecord, cellResult));
        }
      }
      return emptyRecords;
    } catch (SQLException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  @Nullable
  private PathTrialRecord findRecordWithModes(Collection<PathTrialRecord> records, Set<ModeType> modeTypes) {
    for (PathTrialRecord record : records) {
      if (record.modes()
          .stream()
          .map(PathTrialModeRecord::modeType)
          .allMatch(modeTypes::contains)) {
        return record;
      }
    }
    return null;
  }

  @Override
  public PathTrialRecord getRecord(Cell origin, Cell destination, Set<ModeType> modeTypes) {
    return findRecordWithModes(getRecords(origin, destination), modeTypes);
  }

  private Cell toClass(PathTrialCellRecord cellRecord) {
    return new Cell(cellRecord.x(),
        cellRecord.y(),
        cellRecord.z(),
        cellRecord.record().worldId());
  }

  @Override
  public Path getPath(Cell origin, Cell destination, Set<ModeType> modeTypeGroup) {
    try (Connection connection = getConnectionController().establishConnection()) {
      PathTrialRecord record = findRecordWithModes(getRecordsWithoutCells(origin, destination),
          modeTypeGroup);

      ResultSet cellResult = connection.prepareStatement("SELECT * FROM "
          + PATH_RECORD_CELL_TABLE_NAME
          + " WHERE "
          + "path_record_id = " + record.id() + " AND "
          + "critical = TRUE").executeQuery();
      while (cellResult.next()) {
        record.cells().add(extractCell(record, cellResult));
      }

      record.cells().sort(Comparator.comparing(PathTrialCellRecord::index));


      LinkedList<Step> steps = new LinkedList<>();

      // Add the first one because we don't move to get here
      steps.add(new Step(toClass(record.cells().get(0)), 0, record.cells().get(0).modeType()));
      for (int i = 1; i < record.cells().size(); i++) {
        Cell cell = toClass(record.cells().get(i));
        steps.add(new Step(cell,
            cell.distanceTo(steps.getLast().location()),
            record.cells().get(i).modeType()));
      }

      return new Path(steps.getFirst().location(), steps, record.pathLength());
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public boolean containsRecord(Cell origin, Cell destination, Set<ModeType> modeTypeGroup) {
    try (Connection connection = getConnectionController().establishConnection()) {
      return findRecordWithModes(getRecordsWithoutCells(origin, destination), modeTypeGroup) != null;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public @NotNull Collection<PathTrialCellRecord> getAllCells() {
    try (Connection connection = getConnectionController().establishConnection()) {
      ResultSet result = connection.prepareStatement("SELECT * FROM "
          + PATH_RECORD_TABLE_NAME + " "
          + "JOIN " + PATH_RECORD_CELL_TABLE_NAME + " "
          + "ON " + PATH_RECORD_TABLE_NAME + ".id = "
          + PATH_RECORD_CELL_TABLE_NAME + ".path_record_id"
          + ";").executeQuery();
      List<PathTrialCellRecord> cells = new LinkedList<>();
      Map<Long, PathTrialRecord> records = new HashMap<>();
      while (result.next()) {
        PathTrialRecord record = extractRecord(result);
        if (records.containsKey(record.id())) {
          record = records.get(record.id());
        } else {
          records.put(record.id(), record);
        }
        cells.add(extractCell(record, result));
      }
      return cells;
    } catch (SQLException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  private PathTrialRecord extractRecord(final ResultSet resultSet) throws SQLException {
    return new PathTrialRecord(
        resultSet.getLong("id"),
        resultSet.getDate("timestamp"),
        resultSet.getLong("duration"),
        resultSet.getLong("path_length"),
        resultSet.getInt("origin_x"),
        resultSet.getInt("origin_y"),
        resultSet.getInt("origin_z"),
        resultSet.getInt("destination_x"),
        resultSet.getInt("destination_y"),
        resultSet.getInt("destination_z"),
        resultSet.getString("world_uuid"),
        ScoringFunction.Type.valueOf(resultSet.getString("scoring_function")),
        new LinkedList<>(),
        new LinkedList<>()
    );
  }

  private PathTrialCellRecord extractCell(final PathTrialRecord record,
                                          final ResultSet resultSet) throws SQLException {
    return new PathTrialCellRecord(
        record,
        resultSet.getInt("x"),
        resultSet.getInt("y"),
        resultSet.getInt("z"),
        resultSet.getInt("path_index"),
        ModeType.values()[resultSet.getInt("mode_type")]
    );
  }

  protected void createTables() {
    try (Connection connection = getConnectionController().establishConnection()) {

      // Create table of path trials
      connection.prepareStatement("CREATE TABLE IF NOT EXISTS "
          + PATH_RECORD_TABLE_NAME + " ("
          + "id integer PRIMARY KEY AUTOINCREMENT, "
          + "timestamp integer NOT NULL, "
          + "duration integer NOT NULL, "
          + "path_length double(12, 5) NOT NULL, "
          + "origin_x int(7) NOT NULL,"
          + "origin_y int(7) NOT NULL,"
          + "origin_z int(7) NOT NULL,"
          + "destination_x int(7) NOT NULL,"
          + "destination_y int(7) NOT NULL,"
          + "destination_z int(7) NOT NULL,"
          + "world_uuid char(36) NOT NULL, "
          + "scoring_function varchar(32) NOT NULL"
          + ");").execute();

      connection.prepareStatement("CREATE INDEX IF NOT EXISTS path_record_idx ON "
              + PATH_RECORD_TABLE_NAME
              + " (origin_x, origin_y, origin_z, destination_x, destination_y, destination_z, world_uuid);")
          .execute();

      // Create table of nodes within the path trial calculation
      connection.prepareStatement("CREATE TABLE IF NOT EXISTS "
          + PATH_RECORD_CELL_TABLE_NAME + " ("
          + "path_record_id integer NOT NULL, "  // id of saved path trial (indexed)
          + "x int(7) NOT NULL,"  // x coordinate
          + "y int(7) NOT NULL,"  // y coordinate
          + "z int(7) NOT NULL,"  // z coordinate
          + "critical int(1) NOT NULL, "  // is this on the critical path (solution)
          + "path_index int(10), "  // what is the index of this critical node (if not critical, null)
          + "mode_type int(2) NOT NULL, "  // what is the mode type used to get here
          + "deviation double(12, 5) NOT NULL,"
          + "distance double(12, 5) NOT NULL,"  // the euclidean distance
          + "distance_y int(3) NOT NULL,"  // the cartesian distance in the y axis
          + "biome int NOT NULL, "  // the index of the biome
          + "dimension int NOT NULL, "  // the dimension index
          + "random double(12, 12) NOT NULL, "  // a random double for random selection (indexed)
          + "FOREIGN KEY (path_record_id) REFERENCES " + PATH_RECORD_TABLE_NAME + "(id)"
          + " ON DELETE CASCADE"
          + " ON UPDATE CASCADE"
          + ");").execute();

      connection.prepareStatement("CREATE INDEX IF NOT EXISTS cell_path_record_id_idx ON "
          + PATH_RECORD_CELL_TABLE_NAME
          + " (path_record_id);").execute();

      connection.prepareStatement("CREATE INDEX IF NOT EXISTS cell_random_idx ON "
          + PATH_RECORD_CELL_TABLE_NAME
          + " (random);").execute();

      connection.prepareStatement("CREATE TABLE IF NOT EXISTS "
          + PATH_RECORD_MODE_TABLE_NAME + " ("
          + "path_record_id integer NOT NULL, "
          + "mode_type int(2) NOT NULL, "
          + "FOREIGN KEY (path_record_id) REFERENCES " + PATH_RECORD_TABLE_NAME + "(id)"
          + " ON DELETE CASCADE"
          + " ON UPDATE CASCADE, "
          + "UNIQUE(path_record_id, mode_type)"
          + ");").execute();

      connection.prepareStatement("CREATE INDEX IF NOT EXISTS mode_path_record_id_idx ON "
          + PATH_RECORD_MODE_TABLE_NAME
          + " (path_record_id);").execute();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
