package edu.whimc.indicator.spigot.data.sql.sqlite;

import edu.whimc.indicator.common.data.sql.sqlite.SqliteCustomEndpointManager;
import edu.whimc.indicator.spigot.data.SpigotDataConverter;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import org.bukkit.World;

public class SpigotSQLiteCustomEndpointManager extends SqliteCustomEndpointManager<LocationCell, World> {

  /**
   * General constructor.
   */
  public SpigotSQLiteCustomEndpointManager(String address) {
    super(address, new SpigotDataConverter());
  }
}
