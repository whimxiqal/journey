package edu.whimc.journey.spigot.data.sql.mysql;

import edu.whimc.journey.common.data.sql.mysql.MySQLCustomEndpointManager;
import edu.whimc.journey.spigot.data.SpigotDataConverter;
import edu.whimc.journey.spigot.navigation.LocationCell;
import org.bukkit.World;

public class SpigotMySQLCustomEndpointManager extends MySQLCustomEndpointManager<LocationCell, World> {

  public SpigotMySQLCustomEndpointManager() {
    super(new SpigotDataConverter());
  }

}
