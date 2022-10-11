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

package me.pietelite.journey.spigot.data;

import me.pietelite.journey.common.config.Settings;
import me.pietelite.journey.common.data.DataManager;
import me.pietelite.journey.common.data.PathRecordManager;
import me.pietelite.journey.common.data.PersonalWaypointManager;
import me.pietelite.journey.common.data.PublicEndpointManager;
import me.pietelite.journey.spigot.JourneySpigot;
import me.pietelite.journey.spigot.data.sql.mysql.SpigotMySqlPersonalWaypointManager;
import me.pietelite.journey.spigot.data.sql.mysql.SpigotMySqlPublicWaypointManager;
import me.pietelite.journey.spigot.data.sql.sqlite.SpigotSqlitePathRecordManager;
import me.pietelite.journey.spigot.data.sql.sqlite.SpigotSqlitePersonalWaypointManager;
import me.pietelite.journey.spigot.data.sql.sqlite.SpigotSqlitePublicWaypointManager;

/**
 * Implementation of the {@link DataManager} in Spigot Minecraft.
 */
public class SpigotDataManager implements DataManager {

  private final PersonalWaypointManager personalWaypointManager;
  private final PublicEndpointManager publicEndpointManager;
  private final PathRecordManager pathRecordManager;

  /**
   * General constructor.
   */
  public SpigotDataManager() {
    String sqliteAddress = "jdbc:sqlite:" + JourneySpigot.getInstance()
        .getDataFolder()
        .getPath() + "/journey.db";
    switch (Settings.CUSTOM_ENDPOINT_STORAGE_TYPE.getValue()) {
      case SQLITE:
        personalWaypointManager = new SpigotSqlitePersonalWaypointManager(sqliteAddress);
        break;
      case MYSQL:
        personalWaypointManager = new SpigotMySqlPersonalWaypointManager();
        break;
      default:
        JourneySpigot.getInstance()
            .getLogger()
            .severe("This type of custom endpoint storage type is not supported: "
                + Settings.CUSTOM_ENDPOINT_STORAGE_TYPE.getValue()
                + ". Defaulting to SQLite storage.");
        personalWaypointManager = new SpigotSqlitePersonalWaypointManager(sqliteAddress);
    }

    switch (Settings.SERVER_ENDPOINT_STORAGE_TYPE.getValue()) {
      case SQLITE:
        publicEndpointManager = new SpigotSqlitePublicWaypointManager(sqliteAddress);
        break;
      case MYSQL:
        publicEndpointManager = new SpigotMySqlPublicWaypointManager();
        break;
      default:
        JourneySpigot.getInstance()
            .getLogger()
            .severe("This type of server endpoint storage type is not supported: "
                + Settings.CUSTOM_ENDPOINT_STORAGE_TYPE.getValue()
                + ". Defaulting to SQLite storage.");
        publicEndpointManager = new SpigotSqlitePublicWaypointManager(sqliteAddress);
    }

    pathRecordManager = new SpigotSqlitePathRecordManager(sqliteAddress);
  }

  @Override
  public PersonalWaypointManager personalEndpointManager() {
    return personalWaypointManager;
  }

  @Override
  public PublicEndpointManager publicEndpointManager() {
    return publicEndpointManager;
  }

  @Override
  public PathRecordManager pathRecordManager() {
    return pathRecordManager;
  }
}
