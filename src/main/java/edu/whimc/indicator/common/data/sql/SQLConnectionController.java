package edu.whimc.indicator.common.data.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLConnectionController {

  Connection establishConnection() throws SQLException;

}
