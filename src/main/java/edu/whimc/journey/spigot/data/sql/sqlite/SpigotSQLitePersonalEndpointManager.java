package edu.whimc.journey.spigot.data.sql.sqlite;

import edu.whimc.journey.common.data.sql.sqlite.SqlitePersonalEndpointManager;
import edu.whimc.journey.spigot.data.SpigotDataAdapter;
import edu.whimc.journey.spigot.navigation.LocationCell;
import org.bukkit.World;

public class SpigotSQLitePersonalEndpointManager extends SqlitePersonalEndpointManager<LocationCell, World> {

  /**
   * General constructor.
   */
  public SpigotSQLitePersonalEndpointManager(String address) {
    super(address, new SpigotDataAdapter());
  }
}
