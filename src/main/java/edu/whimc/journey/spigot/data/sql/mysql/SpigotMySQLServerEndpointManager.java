package edu.whimc.journey.spigot.data.sql.mysql;

import edu.whimc.journey.common.data.sql.mysql.MySQLServerEndpointManager;
import edu.whimc.journey.spigot.data.SpigotDataConverter;
import edu.whimc.journey.spigot.navigation.LocationCell;
import org.bukkit.World;

public class SpigotMySQLServerEndpointManager extends MySQLServerEndpointManager<LocationCell, World> {
  public SpigotMySQLServerEndpointManager() {
    super(new SpigotDataConverter());
  }
}
