//package edu.whimc.indicator.common.data;
//
//import edu.whimc.indicator.common.path.Cell;
//import edu.whimc.indicator.config.Settings;
//import org.jetbrains.annotations.NotNull;
//
//import java.io.Serializable;
//import java.sql.*;
//import java.util.*;
//import java.util.function.Function;
//
//public abstract class MySQLCustomEndpointDataManager<T extends Cell<T, D, F>, D, F extends Function<String, D> & Serializable> implements DataManager.CustomEndpoint<T, D, F> {
//
//  private static final String CUSTOM_ENDPOINT_TABLE_NAME = "custom_endpoints";
//
//  private final String address;
//  private final Properties databaseProperties;
//
//  /**
//   * General constructor.
//   */
//  public MySQLCustomEndpointDataManager() {
//    address = String.format("jdbc:mysql://%s/%s",
//        Settings.STORAGE_ADDRESS.getValue(),
//        Settings.STORAGE_DATABASE.getValue());
//    databaseProperties = new Properties();
//    databaseProperties.setProperty("user", Settings.STORAGE_USERNAME.getValue());
//    databaseProperties.setProperty("password", Settings.STORAGE_PASSWORD.getValue());
//    createTables();
//  }
//
//  @Override
//  public void addCustomEndpoint(UUID playerUuid, T cell) {
//    try (Connection connection = DriverManager.getConnection(address, databaseProperties)) {
//      PreparedStatement statement = connection.prepareStatement(String.format(
//          "INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?);",
//          CUSTOM_ENDPOINT_TABLE_NAME,
//          "player_uuid",
//          "x",
//          "y",
//          "z",
//          "world_uuid",
//          "timestamp"));
//
//      statement.setString(1, playerUuid.toString());
//      statement.setInt(2, cell.getX());
//      statement.setInt(3, cell.getY());
//      statement.setInt(4, cell.getZ());
//      statement.setString(5, getDomainIdentifier(cell.getDomain()));
//      statement.setLong(6, System.currentTimeMillis() / 1000);
//
//      statement.execute();
//    } catch (SQLException e) {
//      e.printStackTrace();
//    }
//  }
//
//  @Override
//  public void removeCustomEndpoint(UUID playerUuid, T cell) {
//    try (Connection connection = DriverManager.getConnection(address, databaseProperties)) {
//      PreparedStatement statement = connection.prepareStatement(String.format(
//          "DELETE FROM %s WHERE %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?;",
//          CUSTOM_ENDPOINT_TABLE_NAME,
//          "player_uuid",
//          "x",
//          "y",
//          "z",
//          "world_uuid"));
//
//      statement.setString(1, playerUuid.toString());
//      statement.setInt(2, cell.getX());
//      statement.setInt(3, cell.getY());
//      statement.setInt(4, cell.getZ());
//      statement.setString(5, getDomainIdentifier(cell.getDomain()));
//
//      statement.execute();
//    } catch (SQLException e) {
//      e.printStackTrace();
//    }
//  }
//
//  @Override
//  public boolean hasCustomEndpoint(UUID playerUuid, T cell) {
//    try (Connection connection = DriverManager.getConnection(address, databaseProperties)) {
//      PreparedStatement statement = connection.prepareStatement(String.format(
//          "SELECT * FROM %s WHERE %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?;",
//          CUSTOM_ENDPOINT_TABLE_NAME,
//          "player_uuid",
//          "x",
//          "y",
//          "z",
//          "world_uuid"));
//
//      statement.setString(1, playerUuid.toString());
//      statement.setInt(2, cell.getX());
//      statement.setInt(3, cell.getY());
//      statement.setInt(4, cell.getZ());
//      statement.setString(5, getDomainIdentifier(cell.getDomain()));
//
//      return statement.executeQuery().next();
//    } catch (SQLException e) {
//      e.printStackTrace();
//      return false;
//    }
//  }
//
//  @Override
//  public List<T> getCustomEndpoints(UUID playerUuid) {
//    try (Connection connection = DriverManager.getConnection(address, databaseProperties)) {
//      PreparedStatement statement = connection.prepareStatement(String.format(
//          "SELECT * FROM %s WHERE %s = ?;",
//          CUSTOM_ENDPOINT_TABLE_NAME,
//          "player_uuid"));
//
//      statement.setString(1, playerUuid.toString());
//
//      ResultSet resultSet = statement.executeQuery();
//      List<T> customEndpoints = new LinkedList<>();
//      Map<T, Long> timestampMap = new HashMap<>(); // for sorting by time
//      while (resultSet.next()) {
//        T cell = makeCell(resultSet.getInt(2),
//            resultSet.getInt(3),
//            resultSet.getInt(4),
//            resultSet.getString(5));
//        customEndpoints.add(cell);
//        timestampMap.put(cell, resultSet.getLong(6));
//      }
//      customEndpoints.sort(Comparator.comparing(timestampMap::get));
//      return customEndpoints;
//    } catch (SQLException e) {
//      e.printStackTrace();
//      return new LinkedList<>();
//    }
//  }
//
//  private void createTables() {
//    try (Connection connection = DriverManager.getConnection(address, databaseProperties)) {
//      String profiles = "CREATE TABLE IF NOT EXISTS "
//          + CUSTOM_ENDPOINT_TABLE_NAME + " ("
//          + "player_uuid char(36) NOT NULL, "
//          + "x int(7), "
//          + "y int(7), "
//          + "z int(7), "
//          + "world_uuid char(36) NOT NULL, "
//          + "timestamp bigint"
//          + ");";
//      connection.prepareStatement(profiles).execute();
//    } catch (SQLException e) {
//      e.printStackTrace();
//    }
//  }
//
//  @NotNull
//  protected abstract String getDomainIdentifier(@NotNull D domain);
//
//  @NotNull
//  protected abstract T makeCell(int x, int y, int z, @NotNull String domainId);
//
//}
