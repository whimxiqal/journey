package edu.whimc.indicator.common.data.sql.sqlite;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.data.sql.SQLConnectionController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnectionController implements SQLConnectionController {

  private final String address = "jdbc:sqlite:" + Indicator.getInstance().getDataFolder().getPath() + "/indicator.db";

  @Override
  public final Connection establishConnection() throws SQLException {
    return DriverManager.getConnection(address);
  }

}
