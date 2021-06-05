package edu.whimc.indicator.common.data;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.path.Cell;
import edu.whimc.indicator.common.util.Validator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.sql.*;
import java.util.*;
import java.util.function.Function;

public abstract class SQLiteCustomEndpointDataManager<T extends Cell<T, D, F>, D, F extends Function<String, D> & Serializable> implements DataManager.CustomEndpoint<T, D, F> {

  private static final String CUSTOM_ENDPOINT_TABLE_NAME = "custom_endpoints";

  private final String address;

  /**
   * General constructor.
   */
  public SQLiteCustomEndpointDataManager() {
    address = "jdbc:sqlite:" + Indicator.getInstance().getDataFolder().getPath() + "/indicator.db";
    createTables();
  }


  @Override
  public void addCustomEndpoint(UUID playerUuid, T cell) throws IllegalArgumentException {
    try (Connection connection = DriverManager.getConnection(address)) {
      int endpointCount = getCustomEndpoints(playerUuid, connection).size();
      addCustomEndpoint(playerUuid, cell, String.valueOf(endpointCount + 1), connection, true);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  @Override
  public void addCustomEndpoint(UUID playerUuid, T cell, String name) throws IllegalArgumentException {
    try (Connection connection = DriverManager.getConnection(address)) {
      addCustomEndpoint(playerUuid, cell, name, connection, false);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void addCustomEndpoint(UUID playerUuid, T cell, String name, Connection connection, boolean forceValidName) throws SQLException {
    if (!forceValidName && !Validator.isValidDataName(name)) {
      throw new IllegalArgumentException("The given name is not valid: " + name);
    }
    PreparedStatement statement = connection.prepareStatement(String.format(
        "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
        CUSTOM_ENDPOINT_TABLE_NAME,
        "player_uuid",
        "name_id",
        "name",
        "world_uuid",
        "x",
        "y",
        "z",
        "timestamp"));

    statement.setString(1, playerUuid.toString());
    statement.setString(2, name);
    statement.setString(3, name.toLowerCase());
    statement.setString(4, getDomainIdentifier(cell.getDomain()));
    statement.setInt(5, cell.getX());
    statement.setInt(6, cell.getY());
    statement.setInt(7, cell.getZ());
    statement.setLong(8, System.currentTimeMillis() / 1000);

    statement.execute();

  }

  @Override
  public void removeCustomEndpoint(UUID playerUuid, T cell) {
    try (Connection connection = DriverManager.getConnection(address)) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE FROM %s WHERE %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?;",
          CUSTOM_ENDPOINT_TABLE_NAME,
          "player_uuid",
          "world_uuid",
          "x",
          "y",
          "z"));

      statement.setString(1, playerUuid.toString());
      statement.setString(2, getDomainIdentifier(cell.getDomain()));
      statement.setInt(3, cell.getX());
      statement.setInt(4, cell.getY());
      statement.setInt(5, cell.getZ());

      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void removeCustomEndpoint(UUID playerUuid, String name) throws IllegalArgumentException {
    try (Connection connection = DriverManager.getConnection(address)) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE FROM %s WHERE %s = ? AND %s = ?;",
          CUSTOM_ENDPOINT_TABLE_NAME,
          "player_uuid",
          "name_id"));

      statement.setString(1, playerUuid.toString());
      statement.setString(2, name.toLowerCase());

      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean hasCustomEndpoint(UUID playerUuid, T cell) {
    try (Connection connection = DriverManager.getConnection(address)) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT * FROM %s WHERE %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?;",
          CUSTOM_ENDPOINT_TABLE_NAME,
          "player_uuid",
          "world_uuid",
          "x",
          "y",
          "z"));

      statement.setString(1, playerUuid.toString());
      statement.setString(2, getDomainIdentifier(cell.getDomain()));
      statement.setInt(3, cell.getX());
      statement.setInt(4, cell.getY());
      statement.setInt(5, cell.getZ());

      return statement.executeQuery().next();
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  @Nullable
  public T getCustomEndpoint(UUID playerUuid, String name) {
    try (Connection connection = DriverManager.getConnection(address)) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT * FROM %s WHERE %s = ? AND %s = ?;",
          CUSTOM_ENDPOINT_TABLE_NAME,
          "player_uuid",
          "name_id"));

      statement.setString(1, playerUuid.toString());
      statement.setString(2, name.toLowerCase());

      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return makeCell(resultSet.getInt("x"),
            resultSet.getInt("y"),
            resultSet.getInt("z"),
            resultSet.getString("world_uuid"));
      } else {
        return null;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public Map<String, T> getCustomEndpoints(UUID playerUuid) {
    try (Connection connection = DriverManager.getConnection(address)) {
      return getCustomEndpoints(playerUuid, connection);
    } catch (SQLException e) {
      e.printStackTrace();
      return new HashMap<>();
    }
  }

  /**
   * Get custom endpoints on an pre-established connection to save database connection time
   *
   * @param playerUuid the player' uuid
   * @param connection the connection object
   * @return the map of custom endpoints
   * @throws SQLException if sql error occurs
   */
  private Map<String, T> getCustomEndpoints(UUID playerUuid, Connection connection) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(String.format(
        "SELECT * FROM %s WHERE %s = ?;",
        CUSTOM_ENDPOINT_TABLE_NAME,
        "player_uuid"));

    statement.setString(1, playerUuid.toString());

    ResultSet resultSet = statement.executeQuery();
    Map<String, T> customEndpoints = new HashMap<>();
    while (resultSet.next()) {
      customEndpoints.put(resultSet.getString("name"),
          makeCell(resultSet.getInt("x"),
              resultSet.getInt("y"),
              resultSet.getInt("z"),
              resultSet.getString("world_uuid")));
    }
    return customEndpoints;
  }

  private void createTables() {
    try (Connection connection = DriverManager.getConnection(address)) {
      String tableStatement = "CREATE TABLE IF NOT EXISTS "
          + CUSTOM_ENDPOINT_TABLE_NAME + " ("
          + "player_uuid char(36) NOT NULL, "
          + "name_id varchar(32) NOT NULL, "
          + "name varchar(32) NOT NULL, "
          + "world_uuid char(36) NOT NULL, "
          + "x int(7), "
          + "y int(7), "
          + "z int(7), "
          + "timestamp integer"
          + ");";
      connection.prepareStatement(tableStatement).execute();

      String indexStatement = "CREATE INDEX IF NOT EXISTS player_uuid_idx ON "
          + CUSTOM_ENDPOINT_TABLE_NAME
          + " (player_uuid);";
      connection.prepareStatement(indexStatement).execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @NotNull
  protected abstract String getDomainIdentifier(@NotNull D domain);

  @NotNull
  protected abstract T makeCell(int x, int y, int z, @NotNull String domainId);

}
