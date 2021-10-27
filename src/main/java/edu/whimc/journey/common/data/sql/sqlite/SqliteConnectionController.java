package edu.whimc.journey.common.data.sql.sqlite;

import edu.whimc.journey.common.data.sql.SqlConnectionController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteConnectionController implements SqlConnectionController {

  private final String address;

  public SqliteConnectionController(String address) {
    this.address = address;
  }

  @Override
  public final Connection establishConnection() throws SQLException {
    return DriverManager.getConnection(address);
  }

}
