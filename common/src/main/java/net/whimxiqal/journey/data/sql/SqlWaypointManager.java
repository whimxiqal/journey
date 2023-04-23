/*
 * MIT License
 *
 * Copyright (c) whimxiqal
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

package net.whimxiqal.journey.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.data.DataAccessException;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.util.UUIDUtil;
import net.whimxiqal.journey.util.Validator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A manager for storing endpoints of search sessions in SQL.
 */
public abstract class SqlWaypointManager extends SqlManager {

  /**
   * Default constructor.
   *
   * @param connectionController a controller for connecting to a SQL database
   */
  public SqlWaypointManager(SqlConnectionController connectionController) {
    super(connectionController);
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
        SqlManager.WAYPOINTS_TABLE,
        "player_uuid",
        "name_id",
        "name",
        "domain_id",
        "x",
        "y",
        "z",
        "created",
        "publicity"));

    statement.setBytes(1, playerUuid == null ? null : UUIDUtil.uuidToBytes(playerUuid));
    statement.setString(2, name.toLowerCase());
    statement.setString(3, name);
    statement.setBytes(4, UUIDUtil.uuidToBytes(Journey.get().domainManager().domainId(cell.domain())));
    statement.setInt(5, cell.blockX());
    statement.setInt(6, cell.blockY());
    statement.setInt(7, cell.blockZ());
    statement.setLong(8, System.currentTimeMillis() / 1000);
    statement.setBoolean(9, false);

    statement.execute();

  }

  protected void removeWaypoint(@Nullable UUID playerUuid, @NotNull Cell cell) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE FROM %s WHERE %s %s ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?;",
          SqlManager.WAYPOINTS_TABLE,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "domain_id",
          "x",
          "y",
          "z"));

      statement.setBytes(1, playerUuid == null ? null : UUIDUtil.uuidToBytes(playerUuid));
      statement.setBytes(2, UUIDUtil.uuidToBytes(Journey.get().domainManager().domainId(cell.domain())));
      statement.setInt(3, cell.blockX());
      statement.setInt(4, cell.blockY());
      statement.setInt(5, cell.blockZ());

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
          SqlManager.WAYPOINTS_TABLE,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "name_id"));

      statement.setBytes(1, playerUuid == null ? null : UUIDUtil.uuidToBytes(playerUuid));
      statement.setString(2, name.toLowerCase());

      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  protected void renameWaypoint(@Nullable UUID uuid, String name, String newName) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?;",
          SqlManager.WAYPOINTS_TABLE,
          "name_id",
          "player_uuid",
          "name_id"));

      statement.setString(1, newName.toLowerCase(Locale.ENGLISH));
      statement.setBytes(2, uuid == null ? null : UUIDUtil.uuidToBytes(uuid));
      statement.setString(3, name.toLowerCase(Locale.ENGLISH));

      statement.executeUpdate();
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
          SqlManager.WAYPOINTS_TABLE,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "name_id"));

      statement.setBytes(1, playerUuid == null ? null : UUIDUtil.uuidToBytes(playerUuid));
      statement.setString(2, name.toLowerCase());

      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return new Cell(resultSet.getInt("x"),
            resultSet.getInt("y"),
            resultSet.getInt("z"),
            Journey.get().domainManager().domainIndex(UUIDUtil.bytesToUuid(resultSet.getBytes("domain_id"))));
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
          SqlManager.WAYPOINTS_TABLE,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "domain_id",
          "x",
          "y",
          "z"));

      statement.setBytes(1, playerUuid == null ? null : UUIDUtil.uuidToBytes(playerUuid));
      statement.setBytes(2, UUIDUtil.uuidToBytes(Journey.get().domainManager().domainId(cell.domain())));
      statement.setInt(3, cell.blockX());
      statement.setInt(4, cell.blockY());
      statement.setInt(5, cell.blockZ());

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

  protected Map<String, Cell> getWaypoints(@Nullable UUID playerUuid, boolean justPublic) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      return getWaypoints(playerUuid, connection, justPublic);
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
                                         @NotNull Connection connection,
                                         boolean justPublic) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(String.format(
        "SELECT * FROM %s WHERE %s %s ? %s;",
        SqlManager.WAYPOINTS_TABLE,
        "player_uuid",
        playerUuid == null ? "IS" : "=",
        justPublic ? "AND publicity = ?" : ""));

    statement.setBytes(1, playerUuid == null ? null : UUIDUtil.uuidToBytes(playerUuid));
    if (justPublic) {
      statement.setBoolean(2, true);
    }

    ResultSet resultSet = statement.executeQuery();
    Map<String, Cell> endpoints = new HashMap<>();
    while (resultSet.next()) {
      endpoints.put(resultSet.getString("name"),
          new Cell(resultSet.getInt("x"),
              resultSet.getInt("y"),
              resultSet.getInt("z"),
              Journey.get().domainManager().domainIndex(UUIDUtil.bytesToUuid(resultSet.getBytes("domain_id")))));
    }
    return endpoints;
  }

  protected int getWaypointCount(@Nullable UUID playerUuid, boolean justPublic) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT COUNT(*) FROM %s WHERE %s %s ? %s;",
          SqlManager.WAYPOINTS_TABLE,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          justPublic ? "AND publicity = ?" : ""));

      statement.setBytes(1, playerUuid == null ? null : UUIDUtil.uuidToBytes(playerUuid));
      if (justPublic) {
        statement.setBoolean(2, true);
      }

      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return resultSet.getInt(1);
      } else {
        return 0;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

}
