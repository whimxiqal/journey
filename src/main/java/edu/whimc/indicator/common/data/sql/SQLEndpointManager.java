package edu.whimc.indicator.common.data.sql;

import edu.whimc.indicator.common.data.DataAccessException;
import edu.whimc.indicator.common.path.Cell;
import edu.whimc.indicator.common.util.Validator;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;

public abstract class SQLEndpointManager<T extends Cell<T, D>, D> {

  private static final String ENDPOINT_TABLE_NAME = "indicator_endpoints";

  @Getter
  private final SQLConnectionController connectionController;
  @Getter
  private final DataConverter<T, D> dataConverter;

  public SQLEndpointManager(SQLConnectionController connectionController, DataConverter<T, D> dataConverter) {
    this.connectionController = connectionController;
    this.dataConverter = dataConverter;
    createTables();
  }

  protected void addEndpoint(@Nullable UUID playerUuid, @NotNull T cell) throws IllegalArgumentException, DataAccessException {
    try (Connection connection = connectionController.establishConnection()) {
      int endpointCount = getEndpoints(playerUuid, connection).size();
      addEndpoint(playerUuid, cell, String.valueOf(endpointCount + 1), connection, true);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }


  protected void addEndpoint(@Nullable UUID playerUuid,
                             @NotNull T cell,
                             @NotNull String name) throws IllegalArgumentException, DataAccessException {
    try (Connection connection = connectionController.establishConnection()) {
      addEndpoint(playerUuid, cell, name, connection, false);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  private void addEndpoint(@Nullable UUID playerUuid,
                           @NotNull T cell,
                           @NotNull String name,
                           @NotNull Connection connection,
                           boolean forceValidName) throws SQLException {
    if (!forceValidName && !Validator.isValidDataName(name)) {
      throw new IllegalArgumentException("The given name is not valid: " + name);
    }
    PreparedStatement statement = connection.prepareStatement(String.format(
        "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
        ENDPOINT_TABLE_NAME,
        "player_uuid",
        "name_id",
        "name",
        "world_uuid",
        "x",
        "y",
        "z",
        "timestamp"));

    statement.setString(1, playerUuid == null ? null : playerUuid.toString());
    statement.setString(2, name.toLowerCase());
    statement.setString(3, name);
    statement.setString(4, dataConverter.getDomainIdentifier(cell.getDomain()));
    statement.setInt(5, cell.getX());
    statement.setInt(6, cell.getY());
    statement.setInt(7, cell.getZ());
    statement.setLong(8, System.currentTimeMillis() / 1000);

    statement.execute();

  }

  protected void removeEndpoint(@Nullable UUID playerUuid, @NotNull T cell) throws DataAccessException {
    try (Connection connection = connectionController.establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE FROM %s WHERE %s %s ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?;",
          ENDPOINT_TABLE_NAME,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "world_uuid",
          "x",
          "y",
          "z"));

      statement.setString(1, playerUuid == null ? null : playerUuid.toString());
      statement.setString(2, dataConverter.getDomainIdentifier(cell.getDomain()));
      statement.setInt(3, cell.getX());
      statement.setInt(4, cell.getY());
      statement.setInt(5, cell.getZ());

      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  protected void removeEndpoint(@Nullable UUID playerUuid, @NotNull String name) throws DataAccessException {
    try (Connection connection = connectionController.establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE FROM %s WHERE %s %s ? AND %s = ?;",
          ENDPOINT_TABLE_NAME,
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
  protected T getEndpoint(@Nullable UUID playerUuid, @NotNull String name) throws DataAccessException {
    try (Connection connection = connectionController.establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT * FROM %s WHERE %s %s ? AND %s = ?;",
          ENDPOINT_TABLE_NAME,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "name_id"));

      statement.setString(1, playerUuid == null ? null : playerUuid.toString());
      statement.setString(2, name.toLowerCase());

      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return dataConverter.makeCell(resultSet.getInt("x"),
            resultSet.getInt("y"),
            resultSet.getInt("z"),
            resultSet.getString("world_uuid"));
      } else {
        return null;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Nullable
  protected String getEndpointName(@Nullable UUID playerUuid, @NotNull T cell) throws DataAccessException {
    try (Connection connection = connectionController.establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT * FROM %s WHERE %s %s ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?;",
          ENDPOINT_TABLE_NAME,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "world_uuid",
          "x",
          "y",
          "z"));

      statement.setString(1, playerUuid == null ? null : playerUuid.toString());
      statement.setString(2, dataConverter.getDomainIdentifier(cell.getDomain()));
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

  protected Map<String, T> getEndpoints(@Nullable UUID playerUuid) throws DataAccessException {
    try (Connection connection = connectionController.establishConnection()) {
      return getEndpoints(playerUuid, connection);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  /**
   * Get endpoints on an pre-established connection to save database connection time
   *
   * @param playerUuid the player' uuid
   * @param connection the connection object
   * @return the map of endpoints
   * @throws SQLException if sql error occurs
   */
  private Map<String, T> getEndpoints(@Nullable UUID playerUuid,
                                      @NotNull Connection connection) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(String.format(
        "SELECT * FROM %s WHERE %s %s ?;",
        ENDPOINT_TABLE_NAME,
        "player_uuid",
        playerUuid == null ? "IS" : "="));

    statement.setString(1, playerUuid == null ? null : playerUuid.toString());

    ResultSet resultSet = statement.executeQuery();
    Map<String, T> endpoints = new HashMap<>();
    while (resultSet.next()) {
      endpoints.put(resultSet.getString("name"),
          dataConverter.makeCell(resultSet.getInt("x"),
              resultSet.getInt("y"),
              resultSet.getInt("z"),
              resultSet.getString("world_uuid")));
    }
    return endpoints;
  }

  protected void createTables() {
    try (Connection connection = connectionController.establishConnection()) {
      String tableStatement = "CREATE TABLE IF NOT EXISTS "
          + ENDPOINT_TABLE_NAME + " ("
          + "player_uuid char(36), "
          + "name_id varchar(32) NOT NULL, "
          + "name varchar(32) NOT NULL, "
          + "world_uuid char(36) NOT NULL, "
          + "x int(7) NOT NULL, "
          + "y int(7) NOT NULL, "
          + "z int(7) NOT NULL, "
          + "timestamp integer NOT NULL"
          + ");";
      connection.prepareStatement(tableStatement).execute();

      String indexStatement = "CREATE INDEX IF NOT EXISTS player_uuid_idx ON "
          + ENDPOINT_TABLE_NAME
          + " (player_uuid);";
      connection.prepareStatement(indexStatement).execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
