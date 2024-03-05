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

import com.zaxxer.hikari.pool.HikariPool;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.data.sql.SqlPathRecordManager;
import net.whimxiqal.journey.data.sql.SqlPersonalWaypointManager;
import net.whimxiqal.journey.data.sql.SqlPublicWaypointManager;
import net.whimxiqal.journey.data.sql.SqlTunnelDataManager;
import net.whimxiqal.journey.data.sql.mysql.MySqlConnectionController;
import net.whimxiqal.journey.data.sql.sqlite.SqliteConnectionController;
import net.whimxiqal.journey.data.sql.sqlite.SqlitePathRecordManager;
import net.whimxiqal.journey.data.version.DataVersionHandler;
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
    DataVersionHandler versionHandler;
    try {
      switch (Settings.STORAGE_TYPE.getValue()) {
        case SQLITE -> {
          SqliteConnectionController sqliteController = new SqliteConnectionController(Journey.get().proxy().dataFolder().resolve(DATABASE_FILE_NAME).toString());
          personalWaypointManager = new SqlPersonalWaypointManager(sqliteController);
          publicWaypointManager = new SqlPublicWaypointManager(sqliteController);
          pathRecordManager = new SqlitePathRecordManager(sqliteController);  // our sqlite manager will not support records
          tunnelDataManager = new SqlTunnelDataManager(sqliteController);
          versionHandler = new SqliteDataVersionHandler(sqliteController);
        }
        case MYSQL -> {
          MySqlConnectionController mysqlController = new MySqlConnectionController();
          personalWaypointManager = new SqlPersonalWaypointManager(mysqlController);
          publicWaypointManager = new SqlPublicWaypointManager(mysqlController);
          pathRecordManager = new SqlPathRecordManager(mysqlController);
          tunnelDataManager = new SqlTunnelDataManager(mysqlController);
          versionHandler = new MysqlDataVersionHandler(mysqlController);
        }
        default -> throw new RuntimeException();
      }
    } catch (HikariPool.PoolInitializationException e) {
      Journey.logger().error("[Data Manager] Database connection pool initialization failed: " + e.getMessage());
      databaseVersion = DataVersion.ERROR;
      return;
    }

    this.databaseVersion = versionHandler.getVersion();
    if (this.databaseVersion == DataVersion.ERROR) {
      return;
    }

    DataVersion previousVersion = this.databaseVersion;
    boolean updated = false;
    // run migrations until we are fully up-to-date
    while (previousVersion != DataVersion.latest()) {
      this.databaseVersion = versionHandler.runMigration(previousVersion);
      if (this.databaseVersion == DataVersion.ERROR) {
        return;
      }
      if (previousVersion == this.databaseVersion) {
        // no progress was made, quit now
        break;
      }
      updated = true;
      previousVersion = this.databaseVersion;
    }

    if (this.databaseVersion != DataVersion.latest()) {
      Journey.logger().error(String.format("Failed to migrate database beyond %s", this.databaseVersion));
    } else if (updated) {
      Journey.logger().info(String.format("[Data Manager] Updated database to latest version (%s)", this.databaseVersion));
    }

    if (updated) {
      versionHandler.saveVersion(this.databaseVersion);
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
