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

import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.data.DataVersion;
import net.whimxiqal.journey.data.sql.sqlite.SqliteConnectionController;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static net.whimxiqal.journey.data.DataManagerImpl.VERSION_FILE_NAME;
import static net.whimxiqal.journey.data.DataManagerImpl.VERSION_TABLE_NAME;
import static net.whimxiqal.journey.data.DataManagerImpl.VERSION_COLUMN_NAME;

public class SqliteDataVersionHandler extends SqlDataVersionHandler {
    public SqliteDataVersionHandler(SqliteConnectionController controller) {
        super(controller);
    }

    @Override
    public DataVersion version() {
        // If the version file exists we know it's on version 1.
        if (versionFileExists()) return DataVersion.V001;

        // Otherwise, query the database to see what version it's on.
        try (Connection connection = controller.establishConnection()) {
            PreparedStatement statement = connection.prepareStatement(String.format(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='%s'",
                    VERSION_TABLE_NAME
            ));

            ResultSet tableList = statement.executeQuery();
            if (!tableList.next()) return DataVersion.V000;  // The table does not exist, so probably version 0 since there's no version file.

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

    boolean versionFileExists() {

        // THIS FILE IS ONLY HERE FOR BACKWARDS COMPATIBILITY!! It should never be recreated.

        File versionFile = Journey.get().proxy().dataFolder().resolve(VERSION_FILE_NAME).toFile();

        if (versionFile.exists()) {
            Journey.logger().warn("Legacy version file found and deleted, make sure not to add the file back or it could cause issues.");
            versionFile.delete();
            return true;
        }

        return false;
    }

    @Override
    public DataVersion runMigrations(DataVersion currentDataVersion) {
        if (currentDataVersion != DataVersion.V000) {
            Journey.logger().info(String.format("Migrating database version from %s to %s",
                    currentDataVersion,
                    DataVersion.latest()
            ));
        } else Journey.logger().info("No database version found, setting up schema.");

        DataVersion newDataVersion = currentDataVersion;

        // All migration cases should run including and after the current version via fall-through, unless otherwise stopped.
        switch (currentDataVersion) {
            case V000: {
                runBatch("/data/sql/schema/sqlite.sql"); // No data exists, setup schema.
                newDataVersion = DataVersion.latest();
                break;
            }
            case V001: {
                if (runBatch("/data/sql/migration/V001/sqlite.sql")) newDataVersion = DataVersion.V002;
                else break;
            }
            default: { } // Can't migrate from here, nothing needs to be done.
        }

        if (newDataVersion != DataVersion.latest()) Journey.logger().error(String.format("Failed to migrate database past %s!", newDataVersion));
        saveDataVersion(newDataVersion);

        return newDataVersion;
    }
}
