package edu.whimc.indicator.spigot.data;

import edu.whimc.indicator.spigot.IndicatorSpigot;
import edu.whimc.indicator.common.config.Settings;
import edu.whimc.indicator.common.data.CustomEndpointManager;
import edu.whimc.indicator.common.data.DataManager;
import edu.whimc.indicator.common.data.ServerEndpointManager;
import edu.whimc.indicator.spigot.data.sql.mysql.SpigotMySQLCustomEndpointManager;
import edu.whimc.indicator.spigot.data.sql.mysql.SpigotMySQLServerEndpointManager;
import edu.whimc.indicator.spigot.data.sql.sqlite.SpigotSQLiteCustomEndpointManager;
import edu.whimc.indicator.spigot.data.sql.sqlite.SpigotSqliteServerEndpointManager;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import lombok.Getter;
import org.bukkit.World;

public class SpigotDataManager implements DataManager<LocationCell, World> {

  @Getter
  private final CustomEndpointManager<LocationCell, World> customEndpointManager;
  @Getter
  private final ServerEndpointManager<LocationCell, World> serverEndpointManager;

  private final String sqliteAddress = "jdbc:sqlite:" + IndicatorSpigot.getInstance()
      .getDataFolder().getPath() + "/indicator.db";


  public SpigotDataManager() {
    switch (Settings.CUSTOM_ENDPOINT_STORAGE_TYPE.getValue()) {
      case SQLITE -> customEndpointManager = new SpigotSQLiteCustomEndpointManager(sqliteAddress);
      case MYSQL -> customEndpointManager = new SpigotMySQLCustomEndpointManager();
      default -> {
        IndicatorSpigot.getInstance().getLogger().severe("This type of custom endpoint storage type is not supported: "
            + Settings.CUSTOM_ENDPOINT_STORAGE_TYPE.getValue()
            + ". Defaulting to SQLite storage.");
        customEndpointManager = new SpigotSQLiteCustomEndpointManager(sqliteAddress);
      }
    }

    switch (Settings.SERVER_ENDPOINT_STORAGE_TYPE.getValue()) {
      case SQLITE -> serverEndpointManager = new SpigotSqliteServerEndpointManager(sqliteAddress);
      case MYSQL -> serverEndpointManager = new SpigotMySQLServerEndpointManager();
      default -> {
        IndicatorSpigot.getInstance().getLogger().severe("This type of server endpoint storage type is not supported: "
            + Settings.CUSTOM_ENDPOINT_STORAGE_TYPE.getValue()
            + ". Defaulting to SQLite storage.");
        serverEndpointManager = new SpigotSqliteServerEndpointManager(sqliteAddress);
      }
    }
  }

}
