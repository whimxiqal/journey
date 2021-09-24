package edu.whimc.indicator.spigot.data;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.data.CustomEndpointManager;
import edu.whimc.indicator.common.data.DataManager;
import edu.whimc.indicator.common.data.ServerEndpointManager;
import edu.whimc.indicator.common.config.Settings;
import edu.whimc.indicator.spigot.data.sql.mysql.SpigotMySQLCustomEndpointManager;
import edu.whimc.indicator.spigot.data.sql.mysql.SpigotMySQLServerEndpointManager;
import edu.whimc.indicator.spigot.data.sql.sqlite.SpigotSQLiteCustomEndpointManager;
import edu.whimc.indicator.spigot.data.sql.sqlite.SpigotSQLiteServerEndpointManager;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import lombok.Getter;
import org.bukkit.World;

public class SpigotDataManager implements DataManager<LocationCell, World> {

  @Getter
  private final CustomEndpointManager<LocationCell, World> customEndpointManager;
  @Getter
  private final ServerEndpointManager<LocationCell, World> serverEndpointManager;

  public SpigotDataManager() {
    switch (Settings.CUSTOM_ENDPOINT_STORAGE_TYPE.getValue()) {
      case SQLITE:
        customEndpointManager = new SpigotSQLiteCustomEndpointManager();
        break;
      case MYSQL:
        customEndpointManager = new SpigotMySQLCustomEndpointManager();
        break;
      default:
        Indicator.getInstance().getLogger().severe("This type of custom endpoint storage type is not supported: "
            + Settings.CUSTOM_ENDPOINT_STORAGE_TYPE.getValue()
            + ". Defaulting to SQLite storage.");
        customEndpointManager = new SpigotSQLiteCustomEndpointManager();
    }

    switch (Settings.SERVER_ENDPOINT_STORAGE_TYPE.getValue()) {
      case SQLITE:
        serverEndpointManager = new SpigotSQLiteServerEndpointManager();
        break;
      case MYSQL:
        serverEndpointManager = new SpigotMySQLServerEndpointManager();
        break;
      default:
        Indicator.getInstance().getLogger().severe("This type of server endpoint storage type is not supported: "
            + Settings.CUSTOM_ENDPOINT_STORAGE_TYPE.getValue()
            + ". Defaulting to SQLite storage.");
        serverEndpointManager = new SpigotSQLiteServerEndpointManager();
    }
  }

}
