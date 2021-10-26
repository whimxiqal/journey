package edu.whimc.journey.spigot.data.sql.mysql;

import edu.whimc.journey.common.data.sql.mysql.MySQLPersonalEndpointManager;
import edu.whimc.journey.spigot.data.SpigotDataAdapter;
import edu.whimc.journey.spigot.navigation.LocationCell;
import org.bukkit.World;

public class SpigotMySQLPersonalEndpointManager extends MySQLPersonalEndpointManager<LocationCell, World> {

  public SpigotMySQLPersonalEndpointManager() {
    super(new SpigotDataAdapter());
  }

}
