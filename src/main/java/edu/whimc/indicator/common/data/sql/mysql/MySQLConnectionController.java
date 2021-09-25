package edu.whimc.indicator.common.data.sql.mysql;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.config.Settings;
import edu.whimc.indicator.common.data.sql.SQLConnectionController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MySQLConnectionController implements SQLConnectionController {

  private final String address = String.format("jdbc:mysql://%s/%s",
      Settings.STORAGE_ADDRESS.getValue(),
      Settings.STORAGE_DATABASE.getValue());
  private final Properties databaseProperties;

  MySQLConnectionController() {
    databaseProperties = new Properties();
    databaseProperties.setProperty("user", Settings.STORAGE_USERNAME.getValue());
    databaseProperties.setProperty("password", Settings.STORAGE_PASSWORD.getValue());
  }

  @Override
  public final Connection establishConnection() throws SQLException {
    try {
      return DriverManager.getConnection(address, databaseProperties);
    } catch (SQLException e) {
      Indicator.getInstance().getLogger().severe("Could not connect to database. "
          + "Are you sure you are using the correct credentials?");
      throw e;
    }
  }
}
