package edu.whimc.journey.spigot.data.sql.mysql;

import edu.whimc.journey.common.data.sql.mysql.MySqlPersonalEndpointManager;
import edu.whimc.journey.spigot.data.SpigotDataAdapter;
import edu.whimc.journey.spigot.navigation.LocationCell;
import org.bukkit.World;

/**
 * The Spigot implementation of the {@link MySqlPersonalEndpointManager}.
 */
public class SpigotMySqlPersonalEndpointManager extends MySqlPersonalEndpointManager<LocationCell, World> {

  /**
   * General constructor.
   */
  public SpigotMySqlPersonalEndpointManager() {
    super(new SpigotDataAdapter());
  }

}
