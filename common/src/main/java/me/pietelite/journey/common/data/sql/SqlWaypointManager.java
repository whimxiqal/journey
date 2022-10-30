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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.pietelite.journey.common.data.DataAccessException;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.util.Validator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A manager for storing endpoints of search sessions in SQL.
 */
public abstract class SqlWaypointManager extends SqlManager {

  public static final String WAYPOINT_TABLE_NAME = "journey_waypoints";

  /**
   * Default constructor.
   *
   * @param connectionController a controller for connecting to a SQL database
   */
  public SqlWaypointManager(SqlConnectionController connectionController) {
    super(connectionController);
    createTables();
  }

  protected void addWaypoint(@Nullable UUID playerUuid,
                             @NotNull Cell cell,
                             @NotNull String name) throws IllegalArgumentException, DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      addWaypoint(playerUuid, cell, name, connection, false);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  private void addWaypoint(@Nullable UUID playerUuid,
                           @NotNull Cell cell,
                           @NotNull String name,
                           @NotNull Connection connection,
                           boolean forceValidName) throws SQLException {
    if (!forceValidName && Validator.isInvalidDataName(name)) {
      throw new IllegalArgumentException("The given name is not valid: " + name);
    }
    PreparedStatement statement = connection.prepareStatement(String.format(
        "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
        WAYPOINT_TABLE_NAME,
        "player_uuid",
        "name_id",
        "name",
        "domain_id",
        "x",
        "y",
        "z",
        "timestamp",
        "is_public"));

    statement.setString(1, playerUuid == null ? null : playerUuid.toString());
    statement.setString(2, name.toLowerCase());
    statement.setString(3, name);
    statement.setString(4, cell.domainId());
    statement.setInt(5, cell.getX());
    statement.setInt(6, cell.getY());
    statement.setInt(7, cell.getZ());
    statement.setLong(8, System.currentTimeMillis() / 1000);
    statement.setBoolean(9, false);

    statement.execute();

  }

  protected void removeWaypoint(@Nullable UUID playerUuid, @NotNull Cell cell) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE FROM %s WHERE %s %s ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?;",
          WAYPOINT_TABLE_NAME,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "domain_id",
          "x",
          "y",
          "z"));

      statement.setString(1, playerUuid == null ? null : playerUuid.toString());
      statement.setString(2, cell.domainId());
      statement.setInt(3, cell.getX());
      statement.setInt(4, cell.getY());
      statement.setInt(5, cell.getZ());

      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  protected void removeWaypoint(@Nullable UUID playerUuid, @NotNull String name) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE FROM %s WHERE %s %s ? AND %s = ?;",
          WAYPOINT_TABLE_NAME,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "name_id"));

      statement.setString(1, playerUuid == null ? null : playerUuid.toString());
      statement.setString(2, name.toLowerCase());

      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Nullable
  protected Cell getWaypoint(@Nullable UUID playerUuid, @NotNull String name) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT * FROM %s WHERE %s %s ? AND %s = ?;",
          WAYPOINT_TABLE_NAME,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "name_id"));

      statement.setString(1, playerUuid == null ? null : playerUuid.toString());
      statement.setString(2, name.toLowerCase());

      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return new Cell(resultSet.getInt("x"),
            resultSet.getInt("y"),
            resultSet.getInt("z"),
            resultSet.getString("domain_id"));
      } else {
        return null;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }



  @Nullable
  protected String getWaypointName(@Nullable UUID playerUuid, @NotNull Cell cell) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT * FROM %s WHERE %s %s ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?;",
          WAYPOINT_TABLE_NAME,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "domain_id",
          "x",
          "y",
          "z"));

      statement.setString(1, playerUuid == null ? null : playerUuid.toString());
      statement.setString(2, cell.domainId());
      statement.setInt(3, cell.getX());
      statement.setInt(4, cell.getY());
      statement.setInt(5, cell.getZ());

      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return resultSet.getString("name");
      } else {
        return null;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  protected Map<String, Cell> getWaypoints(@Nullable UUID playerUuid) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      return getWaypoints(playerUuid, connection);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  /**
   * Get endpoints on a pre-established connection to save database connection time.
   *
   * @param playerUuid the player' uuid
   * @param connection the connection object
   * @return the map of endpoints
   * @throws SQLException if sql error occurs
   */
  private Map<String, Cell> getWaypoints(@Nullable UUID playerUuid,
                                         @NotNull Connection connection) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(String.format(
        "SELECT * FROM %s WHERE %s %s ?;",
        WAYPOINT_TABLE_NAME,
        "player_uuid",
        playerUuid == null ? "IS" : "="));

    statement.setString(1, playerUuid == null ? null : playerUuid.toString());

    ResultSet resultSet = statement.executeQuery();
    Map<String, Cell> endpoints = new HashMap<>();
    while (resultSet.next()) {
      endpoints.put(resultSet.getString("name"),
          new Cell(resultSet.getInt("x"),
              resultSet.getInt("y"),
              resultSet.getInt("z"),
              resultSet.getString("domain_id")));
    }
    return endpoints;
  }

  protected void createTables() {
    try (Connection connection = getConnectionController().establishConnection()) {
      String tableStatement = "CREATE TABLE IF NOT EXISTS "
          + WAYPOINT_TABLE_NAME + " ("
          + "player_uuid char(36), "
          + "name_id varchar(32) NOT NULL, "
          + "name varchar(32) NOT NULL, "
          + "domain_id char(36) NOT NULL, "
          + "x int(7) NOT NULL, "
          + "y int(7) NOT NULL, "
          + "z int(7) NOT NULL, "
          + "timestamp integer NOT NULL"
          + ");";
      connection.prepareStatement(tableStatement).execute();

      String indexStatement = "CREATE INDEX IF NOT EXISTS player_uuid_idx ON "
          + WAYPOINT_TABLE_NAME
          + " (player_uuid);";
      connection.prepareStatement(indexStatement).execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
