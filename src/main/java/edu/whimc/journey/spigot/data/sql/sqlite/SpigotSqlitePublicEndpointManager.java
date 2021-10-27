package edu.whimc.journey.spigot.data.sql.sqlite;

import edu.whimc.journey.common.data.sql.sqlite.SqlitePublicEndpointManager;
import edu.whimc.journey.spigot.data.SpigotDataAdapter;
import edu.whimc.journey.spigot.navigation.LocationCell;
import org.bukkit.World;

/**
 * The Spigot implementation of the {@link SqlitePublicEndpointManager}.
 */
public class SpigotSqlitePublicEndpointManager extends SqlitePublicEndpointManager<LocationCell, World> {

  /**
   * General constructor.
   *
   * @param address the address location of the sqlite database
   */
  public SpigotSqlitePublicEndpointManager(String address) {
    super(address, new SpigotDataAdapter());
  }
}
