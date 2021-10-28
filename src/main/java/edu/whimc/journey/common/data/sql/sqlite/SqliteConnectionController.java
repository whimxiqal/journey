package edu.whimc.journey.common.data.sql.sqlite;

import edu.whimc.journey.common.data.sql.SqlConnectionController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * An SQL connection controller designed for the SQLite engine.
 */
public record SqliteConnectionController(String address) implements SqlConnectionController {

  @Override
  public Connection establishConnection() throws SQLException {
    return DriverManager.getConnection(address);
  }

}
