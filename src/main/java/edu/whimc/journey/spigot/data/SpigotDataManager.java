package edu.whimc.journey.spigot.data;

import edu.whimc.journey.common.config.Settings;
import edu.whimc.journey.common.data.DataManager;
import edu.whimc.journey.common.data.PersonalEndpointManager;
import edu.whimc.journey.common.data.PublicEndpointManager;
import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.data.sql.mysql.SpigotMySQLPersonalEndpointManager;
import edu.whimc.journey.spigot.data.sql.mysql.SpigotMySqlPublicEndpointManager;
import edu.whimc.journey.spigot.data.sql.sqlite.SpigotSQLitePersonalEndpointManager;
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
      case SQLITE -> personalEndpointManager = new SpigotSQLitePersonalEndpointManager(sqliteAddress);
      case MYSQL -> personalEndpointManager = new SpigotMySQLPersonalEndpointManager();
      default -> {
        JourneySpigot.getInstance()
            .getLogger()
            .severe("This type of custom endpoint storage type is not supported: "
                + Settings.CUSTOM_ENDPOINT_STORAGE_TYPE.getValue()
                + ". Defaulting to SQLite storage.");
        personalEndpointManager = new SpigotSQLitePersonalEndpointManager(sqliteAddress);
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
