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

package net.whimxiqal.journey.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.data.sql.SqlPathRecordManager;
import net.whimxiqal.journey.data.sql.SqlPersonalWaypointManager;
import net.whimxiqal.journey.data.sql.SqlTunnelDataManager;
import net.whimxiqal.journey.data.sql.SqlPublicWaypointManager;
import net.whimxiqal.journey.data.sql.mysql.MySqlConnectionController;
import net.whimxiqal.journey.data.sql.sqlite.SqliteConnectionController;
import net.whimxiqal.journey.util.Initializable;

/**
 * Implementation of the {@link DataManager} in Spigot Minecraft.
 */
public class DataManagerImpl implements DataManager {

  public static final String DATABASE_FILE_NAME = "journey.db";
  private PersonalWaypointManager personalWaypointManager;
  private PublicWaypointManager publicWaypointManager;
  private PathRecordManager pathRecordManager;
  private TunnelDataManager tunnelDataManager;

  @Override
  public void initialize() {
    boolean setupSchema = version() == DataVersion.V000;
    switch (Settings.STORAGE_TYPE.getValue()) {
      case SQLITE:
        String sqliteAddress = "jdbc:sqlite:" + Journey.get().proxy().dataFolder() + "/" + DATABASE_FILE_NAME;
        SqliteConnectionController sqliteController = new SqliteConnectionController(sqliteAddress);
        personalWaypointManager = new SqlPersonalWaypointManager(sqliteController);
        publicWaypointManager = new SqlPublicWaypointManager(sqliteController);
        pathRecordManager = new SqlPathRecordManager(sqliteController);
        tunnelDataManager = new SqlTunnelDataManager(sqliteController);
        if (setupSchema) {
          try (Connection connection = sqliteController.establishConnection()) {
            Statement statement = connection.createStatement();
            addBatchesToStatement("/data/sql/schema/sqlite.sql", statement);
            statement.executeBatch();
          } catch (SQLException e) {
            setupSchema = false;
            e.printStackTrace();
          }
        }
        break;
//      case MYSQL:
//        MySqlConnectionController mysqlController = new MySqlConnectionController();
//        personalWaypointManager = new SqlPersonalWaypointManager(mysqlController);
//        publicWaypointManager = new SqlPublicWaypointManager(mysqlController);
//        pathRecordManager = new SqlPathRecordManager(mysqlController);
//        tunnelDataManager = new SqlTunnelDataManager(mysqlController);
//        if (setupSchema) {
//          try (Connection connection = mysqlController.establishConnection()) {
//            Statement statement = connection.createStatement();
//            addBatchesToStatement("/data/sql/schema/mysql.sql", statement);
//            statement.executeBatch();
//          } catch (SQLException e) {
//            setupSchema = false;
//            e.printStackTrace();
//          }
//        }
//        break;
      default:
        throw new RuntimeException();
    }
    if (setupSchema) {
      DataVersion.writeVersion(DataVersion.latest());
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

  @Override
  public DataVersion version() {
    return DataVersion.version();
  }

  @Override
  public PersonalWaypointManager personalWaypointManager() {
    return personalWaypointManager;
  }

  @Override
  public PublicWaypointManager publicWaypointManager() {
    return publicWaypointManager;
  }

  @Override
  public PathRecordManager pathRecordManager() {
    return pathRecordManager;
  }

  @Override
  public TunnelDataManager netherPortalManager() {
    return tunnelDataManager;
  }
}
