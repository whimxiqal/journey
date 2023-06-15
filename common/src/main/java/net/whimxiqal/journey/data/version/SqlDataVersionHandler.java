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

package net.whimxiqal.journey.data.version;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import net.whimxiqal.journey.data.DataVersion;
import net.whimxiqal.journey.data.sql.SqlConnectionController;

import static net.whimxiqal.journey.data.DataManagerImpl.VERSION_COLUMN_NAME;
import static net.whimxiqal.journey.data.DataManagerImpl.VERSION_TABLE_NAME;

public abstract class SqlDataVersionHandler implements DataVersionHandler {

  SqlConnectionController controller;

  SqlDataVersionHandler(SqlConnectionController controller) {
    this.controller = controller;
  }

  @Override
  public void saveVersion(DataVersion version) {
    try (Connection connection = controller.establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(
          String.format("INSERT INTO %s (%s) VALUES (%o);",
              VERSION_TABLE_NAME,
              VERSION_COLUMN_NAME,
              version.internalVersion
          ));
      statement.execute();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean runBatch(String filePath) {
    try (Connection connection = controller.establishConnection()) {
      Statement statement = connection.createStatement();
      addBatchesToStatement(filePath, statement);
      statement.executeBatch();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  private void addBatchesToStatement(String queryResource, Statement statement) throws SQLException {
    InputStream resourceStream = this.getClass().getResourceAsStream(queryResource);
    if (resourceStream == null) {
      throw new NoSuchElementException("Cannot find resource at path " + queryResource);
    }
    for (String query : new BufferedReader(new InputStreamReader(resourceStream))
        .lines().collect(Collectors.joining("\n")).split(";")) {
      statement.addBatch(query);
    }
  }
}
