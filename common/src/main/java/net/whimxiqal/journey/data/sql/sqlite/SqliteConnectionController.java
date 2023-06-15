/*
 * MIT License
 *
 * Copyright (c) whimxiqal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.whimxiqal.journey.data.sql.sqlite;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.data.sql.SqlConnectionController;

/**
 * An SQL connection controller designed for the SQLite engine.
 */
public class SqliteConnectionController implements SqlConnectionController {
  private final HikariDataSource dataSource;


  public SqliteConnectionController(String filePath) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(String.format("jdbc:sqlite:%s", filePath));
    dataSource = new HikariDataSource(config);
  }

  @Override
  public final Connection establishConnection() throws SQLException {
    try {
      return dataSource.getConnection();
    } catch (SQLException e) {
      Journey.get().proxy().logger().error("Could not connect to database. "
          + "Are you sure you are using the correct credentials?");
      throw e;
    }
  }

  @Override
  public String booleanType() {
    return "INTEGER";
  }

}
