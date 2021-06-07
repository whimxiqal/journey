package edu.whimc.indicator.spigot.data.sql.mysql;

import edu.whimc.indicator.common.data.sql.mysql.MySQLServerEndpointManager;
import edu.whimc.indicator.spigot.data.SpigotDataConverter;
import edu.whimc.indicator.spigot.path.LocationCell;
import org.bukkit.World;

public class SpigotMySQLServerEndpointManager extends MySQLServerEndpointManager<LocationCell, World> {
  public SpigotMySQLServerEndpointManager() {
    super(new SpigotDataConverter());
  }
}
