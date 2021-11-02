/*
 * MIT License
 *
 * Copyright 2021 Pieter Svenson
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
 *
 */

package edu.whimc.journey.spigot.data;

import edu.whimc.journey.common.config.Settings;
import edu.whimc.journey.common.data.DataManager;
import edu.whimc.journey.common.data.PersonalEndpointManager;
import edu.whimc.journey.common.data.PublicEndpointManager;
import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.data.sql.mysql.SpigotMySqlPersonalEndpointManager;
import edu.whimc.journey.spigot.data.sql.mysql.SpigotMySqlPublicEndpointManager;
import edu.whimc.journey.spigot.data.sql.sqlite.SpigotSqlitePersonalEndpointManager;
import edu.whimc.journey.spigot.data.sql.sqlite.SpigotSqlitePublicEndpointManager;
import edu.whimc.journey.spigot.navigation.LocationCell;
import org.bukkit.World;

/**
 * Implementation of the {@link DataManager} in Spigot Minecraft.
 */
public class SpigotDataManager implements DataManager<LocationCell, World> {

  private final PersonalEndpointManager<LocationCell, World> personalEndpointManager;
  private final PublicEndpointManager<LocationCell, World> publicEndpointManager;

  /**
   * General constructor.
   */
  public SpigotDataManager() {
    String sqliteAddress = "jdbc:sqlite:" + JourneySpigot.getInstance()
        .getDataFolder()
        .getPath() + "/journey.db";
    switch (Settings.CUSTOM_ENDPOINT_STORAGE_TYPE.getValue()) {
      case SQLITE -> personalEndpointManager = new SpigotSqlitePersonalEndpointManager(sqliteAddress);
      case MYSQL -> personalEndpointManager = new SpigotMySqlPersonalEndpointManager();
      default -> {
        JourneySpigot.getInstance()
            .getLogger()
            .severe("This type of custom endpoint storage type is not supported: "
                + Settings.CUSTOM_ENDPOINT_STORAGE_TYPE.getValue()
                + ". Defaulting to SQLite storage.");
        personalEndpointManager = new SpigotSqlitePersonalEndpointManager(sqliteAddress);
      }
    }

    switch (Settings.SERVER_ENDPOINT_STORAGE_TYPE.getValue()) {
      case SQLITE -> publicEndpointManager = new SpigotSqlitePublicEndpointManager(sqliteAddress);
      case MYSQL -> publicEndpointManager = new SpigotMySqlPublicEndpointManager();
      default -> {
        JourneySpigot.getInstance()
            .getLogger()
            .severe("This type of server endpoint storage type is not supported: "
                + Settings.CUSTOM_ENDPOINT_STORAGE_TYPE.getValue()
                + ". Defaulting to SQLite storage.");
        publicEndpointManager = new SpigotSqlitePublicEndpointManager(sqliteAddress);
      }
    }
  }

  @Override
  public PersonalEndpointManager<LocationCell, World> getPersonalEndpointManager() {
    return personalEndpointManager;
  }

  @Override
  public PublicEndpointManager<LocationCell, World> getPublicEndpointManager() {
    return publicEndpointManager;
  }
}
