package edu.whimc.journey.common.data.sql.mysql;

import edu.whimc.journey.common.JourneyCommon;
import edu.whimc.journey.common.config.Settings;
import edu.whimc.journey.common.data.sql.SQLConnectionController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MySqlConnectionController implements SQLConnectionController {

  private final String address = String.format("jdbc:mysql://%s/%s",
      Settings.STORAGE_ADDRESS.getValue(),
      Settings.STORAGE_DATABASE.getValue());
  private final Properties databaseProperties;

  MySqlConnectionController() {
    databaseProperties = new Properties();
    databaseProperties.setProperty("user", Settings.STORAGE_USERNAME.getValue());
    databaseProperties.setProperty("password", Settings.STORAGE_PASSWORD.getValue());
  }

  @Override
  public final Connection establishConnection() throws SQLException {
    try {
      return DriverManager.getConnection(address, databaseProperties);
    } catch (SQLException e) {
      JourneyCommon.getLogger().error("Could not connect to database. "
          + "Are you sure you are using the correct credentials?");
      throw e;
    }
  }
}
