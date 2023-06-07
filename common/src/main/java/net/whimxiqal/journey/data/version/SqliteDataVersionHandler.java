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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.data.DataVersion;
import net.whimxiqal.journey.data.sql.sqlite.SqliteConnectionController;

import static net.whimxiqal.journey.data.DataManagerImpl.*;

public class SqliteDataVersionHandler extends SqlDataVersionHandler {
  public SqliteDataVersionHandler(SqliteConnectionController controller) {
    super(controller);
  }

  @Override
  public DataVersion getVersion() {
    // Otherwise, query the database to see what version it's on.
    try (Connection connection = controller.establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT name FROM sqlite_master WHERE type='table' AND name='%s'",
          VERSION_TABLE_NAME
      ));

      ResultSet tableList = statement.executeQuery();
      if (!tableList.next()) {
        // No database version table yet. If the version file exists we know it's on version 1,
        // otherwise it's a new system
        if (legacyVersionFile().exists()) {
          return DataVersion.V001;
        } else {
          return DataVersion.V000;
        }
      }

      PreparedStatement readStatement = connection.prepareStatement(String.format(
          "SELECT Max(%s) as DBVersion FROM %s;",
          VERSION_COLUMN_NAME,
          VERSION_TABLE_NAME
      ));
      ResultSet savedVersions = readStatement.executeQuery();

      if (savedVersions.next()) return DataVersion.fromInt(savedVersions.getInt("DBVersion"));
      else {
        // The table exists, but it does not have a data version, that's bad.
        Journey.logger().error("The " + VERSION_TABLE_NAME + " table exists, but does not contain any data.");
        return DataVersion.ERROR;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // If we get here then there was an error.
    return DataVersion.ERROR;
  }

  private File legacyVersionFile() {
    return Journey.get().proxy().dataFolder().resolve(VERSION_FILE_NAME).toFile();
  }

  @Override
  public DataVersion runMigration(DataVersion currentDataVersion) {
    switch (currentDataVersion) {
      case V000 -> {
        runBatch("/data/sql/schema/sqlite.sql"); // No data exists, setup schema.
        return DataVersion.latest();
      }
      case V001 -> {
        File legacyVersionFile = legacyVersionFile();
        if (legacyVersionFile.exists()) {
          if (!legacyVersionFile.delete()) {
            Journey.logger().error("Tried to update from internal database version " + DataVersion.V001 + " but could not delete legacy version file");
            return currentDataVersion;
          }
        } else {
          Journey.logger().warn("Updating from internal database version " + DataVersion.V001 + " but could not find legacy version file");
        }
        if (runBatch("/data/sql/migration/V001/sqlite.sql")) {
          return DataVersion.V002;
        }
      }
      default -> {}
    }

    return currentDataVersion;
  }
}
