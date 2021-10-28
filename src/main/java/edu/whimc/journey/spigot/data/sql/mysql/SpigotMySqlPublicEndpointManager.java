package edu.whimc.journey.spigot.data.sql.mysql;

import edu.whimc.journey.common.data.sql.mysql.MySqlPublicEndpointManager;
import edu.whimc.journey.spigot.data.SpigotDataAdapter;
import edu.whimc.journey.spigot.navigation.LocationCell;
import org.bukkit.World;

/**
 * SQL public endpoint manager implemented in Spigot Minecraft.
 */
public class SpigotMySqlPublicEndpointManager extends MySqlPublicEndpointManager<LocationCell, World> {

  /**
   * General constructor.
   */
  public SpigotMySqlPublicEndpointManager() {
    super(new SpigotDataAdapter());
  }

}
