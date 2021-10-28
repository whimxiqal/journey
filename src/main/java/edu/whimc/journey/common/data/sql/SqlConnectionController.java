package edu.whimc.journey.common.data.sql;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * An interface for a connection controller for an SQL engine.
 */
public interface SqlConnectionController {

  /**
   * Establish a connection with an SQL engine.
   *
   * @return the connection object
   * @throws SQLException an SQL exception if one occurs.
   */
  Connection establishConnection() throws SQLException;

}
