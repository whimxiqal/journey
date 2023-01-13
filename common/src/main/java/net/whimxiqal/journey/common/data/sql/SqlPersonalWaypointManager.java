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

package net.whimxiqal.journey.common.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import net.whimxiqal.journey.common.data.DataAccessException;
import net.whimxiqal.journey.common.data.PersonalWaypointManager;
import net.whimxiqal.journey.common.navigation.Cell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A combination of an endpoint manager for SQL and a personal endpoint manager.
 */
public class SqlPersonalWaypointManager
    extends SqlWaypointManager
    implements PersonalWaypointManager {

  /**
   * Default constructor.
   *
   * @param connectionController the connection controller
   */
  public SqlPersonalWaypointManager(SqlConnectionController connectionController) {
    super(connectionController);
  }

  @Override
  public void add(@NotNull UUID playerUuid, @NotNull Cell cell, @NotNull String name)
      throws IllegalArgumentException, DataAccessException {
    super.addWaypoint(playerUuid, cell, name);
  }

  @Override
  public void remove(@NotNull UUID playerUuid, @NotNull Cell cell)
      throws DataAccessException {
    super.removeWaypoint(playerUuid, cell);
  }

  @Override
  public void setPublic(@NotNull UUID playerUuid, @NotNull String name, boolean isPublic) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?;",
          WAYPOINT_TABLE_NAME,
          "is_public",
          "player_uuid",
          "name_id"));

      statement.setBoolean(1, isPublic);
      statement.setString(2, playerUuid == null ? null : playerUuid.toString());
      statement.setString(3, name.toLowerCase());

      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Override
  public void remove(@NotNull UUID playerUuid, @NotNull String name)
      throws DataAccessException {
    super.removeWaypoint(playerUuid, name);
  }

  @Override
  public void renameWaypoint(UUID uuid, String name, String newName) throws DataAccessException {
    super.renameWaypoint(uuid, name, newName);
  }

  @Override
  public @Nullable String getName(@NotNull UUID playerUuid, @NotNull Cell cell)
      throws DataAccessException {
    return super.getWaypointName(playerUuid, cell);
  }

  @Override
  public @Nullable Cell getWaypoint(@NotNull UUID playerUuid, @NotNull String name) throws DataAccessException {
    return super.getWaypoint(playerUuid, name);
  }

  @Override
  public @Nullable boolean isPublic(@NotNull UUID playerUuid, @NotNull String name) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT is_public FROM %s WHERE %s = ? AND %s = ?;",
          WAYPOINT_TABLE_NAME,
          "player_uuid",
          "name_id"));

      statement.setString(1, playerUuid.toString());
      statement.setString(2, name.toLowerCase());

      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return resultSet.getBoolean("is_public");
      } else {
        throw new RuntimeException("Checking is-public on non-existent waypoint: " + playerUuid + ", " + name);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Override
  public Map<String, Cell> getAll(@NotNull UUID playerUuid)
      throws DataAccessException {
    return this.getWaypoints(playerUuid);
  }

  @Override
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
          + "timestamp integer NOT NULL, "
          + "is_public " + getConnectionController().booleanType() + " NOT NULL"
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
