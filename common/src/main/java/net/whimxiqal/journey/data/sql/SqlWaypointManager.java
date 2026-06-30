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
import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.data.DataAccessException;
import net.whimxiqal.journey.data.Waypoint;
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
    addWaypoint(playerUuid, cell, name, name);
  }

  protected void addWaypoint(@Nullable UUID playerUuid,
                             @NotNull Cell cell,
                             @NotNull String nameId,
                             @NotNull String displayName) throws IllegalArgumentException, DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      addWaypoint(playerUuid, cell, nameId, displayName, connection, false);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  private void addWaypoint(@Nullable UUID playerUuid,
                           @NotNull Cell cell,
                           @NotNull String nameId,
                           @NotNull String displayName,
                           @NotNull Connection connection,
                           boolean forceValidName) throws SQLException {
    if (!forceValidName && Validator.isInvalidDataName(nameId)) {
      throw new IllegalArgumentException("The given name id is not valid: " + nameId);
    }
    if (!forceValidName && Validator.isInvalidDisplayName(displayName)) {
      throw new IllegalArgumentException("The given display name is not valid: " + displayName);
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
    statement.setString(2, nameId.toLowerCase());
    statement.setString(3, displayName);
    statement.setBytes(4, UUIDUtil.uuidToBytes(Journey.get().domainManager().domainId(cell.domain())));
    statement.setInt(5, cell.blockX());
    statement.setInt(6, cell.blockY());
    statement.setInt(7, cell.blockZ());
    statement.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
    statement.setBoolean(9, false);

    statement.execute();

  }

  private void addWaypoint(@Nullable UUID playerUuid,
                           @NotNull Cell cell,
                           @NotNull String name,
                           @NotNull Connection connection,
                           boolean forceValidName) throws SQLException {
    addWaypoint(playerUuid, cell, name, name, connection, forceValidName);
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
    String nameId = resolveNameId(playerUuid, name);
    if (nameId == null) {
      return;
    }
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE FROM %s WHERE %s %s ? AND %s = ?;",
          SqlManager.WAYPOINTS_TABLE,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "name_id"));

      statement.setBytes(1, playerUuid == null ? null : UUIDUtil.uuidToBytes(playerUuid));
      statement.setString(2, nameId);

      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  protected void renameWaypoint(@Nullable UUID uuid, String name, String newName) throws DataAccessException {
    String nameId = resolveNameId(uuid, name);
    if (nameId == null) {
      return;
    }
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?;",
          SqlManager.WAYPOINTS_TABLE,
          "name_id",
          "player_uuid",
          "name_id"));

      statement.setString(1, newName.toLowerCase(Locale.ENGLISH));
      statement.setBytes(2, uuid == null ? null : UUIDUtil.uuidToBytes(uuid));
      statement.setString(3, nameId);

      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }


  @Nullable
  protected Cell getWaypoint(@Nullable UUID playerUuid, @NotNull String name) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      Cell waypoint = getWaypointByNameId(playerUuid, name.toLowerCase(), connection);
      if (waypoint != null) {
        return waypoint;
      }
      return getWaypointByDisplayName(playerUuid, name, connection);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Nullable
  protected String resolveNameId(@Nullable UUID playerUuid, @NotNull String name) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      String nameId = getNameIdByNameId(playerUuid, name.toLowerCase(), connection);
      if (nameId != null) {
        return nameId;
      }
      return getNameIdByDisplayName(playerUuid, name, connection);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Nullable
  private Cell getWaypointByNameId(@Nullable UUID playerUuid,
                                   @NotNull String nameId,
                                   @NotNull Connection connection) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(String.format(
        "SELECT * FROM %s WHERE %s %s ? AND %s = ?;",
        SqlManager.WAYPOINTS_TABLE,
        "player_uuid",
        playerUuid == null ? "IS" : "=",
        "name_id"));

    statement.setBytes(1, playerUuid == null ? null : UUIDUtil.uuidToBytes(playerUuid));
    statement.setString(2, nameId);

    return readWaypointCell(statement.executeQuery());
  }

  @Nullable
  private Cell getWaypointByDisplayName(@Nullable UUID playerUuid,
                                        @NotNull String displayName,
                                        @NotNull Connection connection) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(String.format(
        "SELECT * FROM %s WHERE %s %s ? AND LOWER(%s) = LOWER(?);",
        SqlManager.WAYPOINTS_TABLE,
        "player_uuid",
        playerUuid == null ? "IS" : "=",
        "name"));

    statement.setBytes(1, playerUuid == null ? null : UUIDUtil.uuidToBytes(playerUuid));
    statement.setString(2, displayName);

    return readWaypointCell(statement.executeQuery());
  }

  @Nullable
  private String getNameIdByNameId(@Nullable UUID playerUuid,
                                   @NotNull String nameId,
                                   @NotNull Connection connection) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(String.format(
        "SELECT %s FROM %s WHERE %s %s ? AND %s = ?;",
        "name_id",
        SqlManager.WAYPOINTS_TABLE,
        "player_uuid",
        playerUuid == null ? "IS" : "=",
        "name_id"));

    statement.setBytes(1, playerUuid == null ? null : UUIDUtil.uuidToBytes(playerUuid));
    statement.setString(2, nameId);

    ResultSet resultSet = statement.executeQuery();
    if (resultSet.next()) {
      return resultSet.getString("name_id");
    }
    return null;
  }

  @Nullable
  private String getNameIdByDisplayName(@Nullable UUID playerUuid,
                                          @NotNull String displayName,
                                          @NotNull Connection connection) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(String.format(
        "SELECT %s FROM %s WHERE %s %s ? AND LOWER(%s) = LOWER(?);",
        "name_id",
        SqlManager.WAYPOINTS_TABLE,
        "player_uuid",
        playerUuid == null ? "IS" : "=",
        "name"));

    statement.setBytes(1, playerUuid == null ? null : UUIDUtil.uuidToBytes(playerUuid));
    statement.setString(2, displayName);

    ResultSet resultSet = statement.executeQuery();
    if (resultSet.next()) {
      return resultSet.getString("name_id");
    }
    return null;
  }

  @Nullable
  private Cell readWaypointCell(ResultSet resultSet) throws SQLException {
    if (resultSet.next()) {
      return new Cell(resultSet.getInt("x"),
          resultSet.getInt("y"),
          resultSet.getInt("z"),
          Journey.get().domainManager().domainIndex(UUIDUtil.bytesToUuid(resultSet.getBytes("domain_id"))));
    }
    return null;
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

  protected List<Waypoint> getWaypoints(@Nullable UUID playerUuid, boolean justPublic) throws DataAccessException {
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
  private List<Waypoint> getWaypoints(@Nullable UUID playerUuid,
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
    List<Waypoint> waypoints = new LinkedList<>();
    while (resultSet.next()) {
      waypoints.add(new Waypoint(resultSet.getString("name_id"),
          resultSet.getString("name"),
          new Cell(resultSet.getInt("x"),
              resultSet.getInt("y"),
              resultSet.getInt("z"),
              Journey.get().domainManager().domainIndex(UUIDUtil.bytesToUuid(resultSet.getBytes("domain_id")))),
          resultSet.getBoolean("publicity")));
    }
    return Collections.unmodifiableList(waypoints);
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
