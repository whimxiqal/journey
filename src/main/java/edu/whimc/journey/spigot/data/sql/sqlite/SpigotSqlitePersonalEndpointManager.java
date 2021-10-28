package edu.whimc.journey.spigot.data.sql.sqlite;

import edu.whimc.journey.common.data.sql.sqlite.SqlitePersonalEndpointManager;
import edu.whimc.journey.spigot.data.SpigotDataAdapter;
import edu.whimc.journey.spigot.navigation.LocationCell;
import org.bukkit.World;

/**
 * The Spigot implementation of the {@link SqlitePersonalEndpointManager}.
 */
public class SpigotSqlitePersonalEndpointManager extends SqlitePersonalEndpointManager<LocationCell, World> {

  /**
   * General constructor.
   */
  public SpigotSqlitePersonalEndpointManager(String address) {
    super(address, new SpigotDataAdapter());
  }
}
