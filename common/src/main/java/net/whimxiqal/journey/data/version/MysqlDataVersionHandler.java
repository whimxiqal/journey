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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.data.DataManagerImpl;
import net.whimxiqal.journey.data.DataVersion;
import net.whimxiqal.journey.data.sql.mysql.MySqlConnectionController;

public class MysqlDataVersionHandler extends SqlDataVersionHandler {
  public MysqlDataVersionHandler(MySqlConnectionController controller) {
    super(controller);
  }

  @Override
  public DataVersion getVersion() {
    try (Connection connection = controller.establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format("SHOW TABLES LIKE '%s'", DataManagerImpl.VERSION_TABLE_NAME));

      ResultSet tableList = statement.executeQuery();
      if (!tableList.next()) {
        // The table does not exist, so must be version 0
        // (We don't have to check for a version file here since it was never used while MySQL existed)
        return DataVersion.V000;
      }

      PreparedStatement readStatement = connection.prepareStatement(String.format("SELECT Max(%s) as DBVersion FROM %s;",
          DataManagerImpl.VERSION_COLUMN_NAME, DataManagerImpl.VERSION_TABLE_NAME));
      ResultSet savedVersions = readStatement.executeQuery();

      if (savedVersions.next()) return DataVersion.fromInt(savedVersions.getInt("DBVersion"));
      else {
        // The table exists, but it does not have a data version, that's bad.
        Journey.logger().error("The " + DataManagerImpl.VERSION_TABLE_NAME + " table exists, but does not contain any data.");
        return DataVersion.ERROR;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // If we get here then there was an error.
    return DataVersion.ERROR;
  }

  @Override
  public DataVersion runMigration(DataVersion currentVersion) {
    switch (currentVersion) {
      case V000 -> {
        runBatch("/data/sql/schema/mysql.sql"); // No data exists, setup schema.
        return DataVersion.latest();
      }
      case V001 -> Journey.logger().error("Tried to update unreachable database version " + DataVersion.V001);
      default -> {
      }
    }
    return currentVersion; // did nothing

  }

}
