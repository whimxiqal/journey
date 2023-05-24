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

import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.data.sql.SqlPathRecordManager;
import net.whimxiqal.journey.data.sql.SqlPersonalWaypointManager;
import net.whimxiqal.journey.data.sql.SqlTunnelDataManager;
import net.whimxiqal.journey.data.sql.SqlPublicWaypointManager;
import net.whimxiqal.journey.data.sql.mysql.MySqlConnectionController;
import net.whimxiqal.journey.data.sql.sqlite.SqliteConnectionController;
import net.whimxiqal.journey.data.version.MysqlDataVersionHandler;
import net.whimxiqal.journey.data.version.SqliteDataVersionHandler;

public class DataManagerImpl implements DataManager {

  private PersonalWaypointManager personalWaypointManager;
  private PublicWaypointManager publicWaypointManager;
  private PathRecordManager pathRecordManager;
  private TunnelDataManager tunnelDataManager;

  public static final String DATABASE_FILE_NAME = "journey.db";

  public static final String VERSION_FILE_NAME = "journeydb.ver";

  public static final String VERSION_TABLE_NAME = "journey_db_version";

  public static final String VERSION_COLUMN_NAME = "db_version";

  private DataVersion databaseVersion = null;

  @Override
  public void initialize() {
    DataVersion currentVersion;
    int currentVersionNumber;

    switch (Settings.STORAGE_TYPE.getValue()) {
      case SQLITE:
        String sqliteAddress = "jdbc:sqlite:" + Journey.get().proxy().dataFolder() + "/" + DATABASE_FILE_NAME;
        SqliteConnectionController sqliteController = new SqliteConnectionController(sqliteAddress);

        SqliteDataVersionHandler sqliteDataVersionHandler = new SqliteDataVersionHandler(sqliteController);

        currentVersion = sqliteDataVersionHandler.version();
        currentVersionNumber = currentVersion.internalVersion;

        personalWaypointManager = new SqlPersonalWaypointManager(sqliteController);
        publicWaypointManager = new SqlPublicWaypointManager(sqliteController);
        pathRecordManager = new SqlPathRecordManager(sqliteController);
        tunnelDataManager = new SqlTunnelDataManager(sqliteController);

        if (currentVersionNumber < DataVersion.latest().internalVersion) {
          databaseVersion = sqliteDataVersionHandler.runMigrations(currentVersion);
        } else databaseVersion = DataVersion.latest();
        break;
      case MYSQL:
        MySqlConnectionController mysqlController = new MySqlConnectionController();

        MysqlDataVersionHandler mysqlDataVersionHandler = new MysqlDataVersionHandler(mysqlController);
        currentVersion = mysqlDataVersionHandler.version();
        currentVersionNumber = currentVersion.internalVersion;

        personalWaypointManager = new SqlPersonalWaypointManager(mysqlController);
        publicWaypointManager = new SqlPublicWaypointManager(mysqlController);
        pathRecordManager = new SqlPathRecordManager(mysqlController);
        tunnelDataManager = new SqlTunnelDataManager(mysqlController);

        if (currentVersionNumber < DataVersion.latest().internalVersion) {
          databaseVersion = mysqlDataVersionHandler.runMigrations(currentVersion);
        } else databaseVersion = DataVersion.latest();
        break;
      default:
        throw new RuntimeException();
    }
  }

  @Override
  public DataVersion version() {
    return databaseVersion;
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
