package edu.whimc.indicator.spigot.data;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.data.DataManager;
import edu.whimc.indicator.config.Settings;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.util.UuidToWorld;
import lombok.Getter;
import org.bukkit.World;

public class SpigotDataManager implements DataManager<LocationCell, World, UuidToWorld> {

  @Getter
  private CustomEndpoint<LocationCell, World, UuidToWorld> customEndpointManager;

  public SpigotDataManager() {
    switch (Settings.CUSTOM_ENDPOINT_STORAGE_TYPE.getValue()) {
      case SQLite:
        customEndpointManager = new SpigotSQLiteCustomEndpointDataManager();
      default:
        Indicator.getInstance().getLogger().severe("This type of custom endpoint storage type is not supported: "
            + Settings.CUSTOM_ENDPOINT_STORAGE_TYPE.getValue()
            + ". Defaulting to SQLite storage.");
        customEndpointManager = new SpigotSQLiteCustomEndpointDataManager();
    }
  }

}
