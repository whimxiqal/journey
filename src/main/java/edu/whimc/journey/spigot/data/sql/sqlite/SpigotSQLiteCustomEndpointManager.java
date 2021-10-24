package edu.whimc.journey.spigot.data.sql.sqlite;

import edu.whimc.journey.common.data.sql.sqlite.SqliteCustomEndpointManager;
import edu.whimc.journey.spigot.data.SpigotDataConverter;
import edu.whimc.journey.spigot.navigation.LocationCell;
import org.bukkit.World;

public class SpigotSQLiteCustomEndpointManager extends SqliteCustomEndpointManager<LocationCell, World> {

  /**
   * General constructor.
   */
  public SpigotSQLiteCustomEndpointManager(String address) {
    super(address, new SpigotDataConverter());
  }
}
