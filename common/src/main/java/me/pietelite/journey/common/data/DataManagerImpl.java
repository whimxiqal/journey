/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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

package me.pietelite.journey.common.data;

import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.config.Settings;
import me.pietelite.journey.common.data.sql.SqlPathRecordManager;
import me.pietelite.journey.common.data.sql.SqlPersonalWaypointManager;
import me.pietelite.journey.common.data.sql.SqlPortDataManager;
import me.pietelite.journey.common.data.sql.SqlPublicWaypointManager;
import me.pietelite.journey.common.data.sql.mysql.MySqlConnectionController;
import me.pietelite.journey.common.data.sql.sqlite.SqliteConnectionController;

/**
 * Implementation of the {@link DataManager} in Spigot Minecraft.
 */
public class DataManagerImpl implements DataManager {

  private PersonalWaypointManager personalWaypointManager;
  private PublicWaypointManager publicWaypointManager;
  private PathRecordManager pathRecordManager;
  private PortDataManager portDataManager;

  public void init() {
    switch (Settings.STORAGE_TYPE.getValue()) {
      case SQLITE:
        String sqliteAddress = "jdbc:sqlite:" + Journey.get().proxy().dataFolder() + "/journey.db";
        SqliteConnectionController sqliteController = new SqliteConnectionController(sqliteAddress);
        personalWaypointManager = new SqlPersonalWaypointManager(sqliteController);
        publicWaypointManager = new SqlPublicWaypointManager(sqliteController);
        pathRecordManager = new SqlPathRecordManager(sqliteController);
        portDataManager = new SqlPortDataManager(sqliteController);
        break;
      case MYSQL:
        MySqlConnectionController mysqlController = new MySqlConnectionController();
        personalWaypointManager = new SqlPersonalWaypointManager(mysqlController);
        publicWaypointManager = new SqlPublicWaypointManager(mysqlController);
        pathRecordManager = new SqlPathRecordManager(mysqlController);
        portDataManager = new SqlPortDataManager(mysqlController);
        break;
      default:
        throw new RuntimeException();
    }
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
  public PortDataManager portManager() {
    return portDataManager;
  }
}
