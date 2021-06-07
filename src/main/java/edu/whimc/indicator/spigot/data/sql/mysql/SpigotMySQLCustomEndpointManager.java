package edu.whimc.indicator.spigot.data.sql.mysql;

import edu.whimc.indicator.common.data.sql.mysql.MySQLCustomEndpointManager;
import edu.whimc.indicator.spigot.data.SpigotDataConverter;
import edu.whimc.indicator.spigot.path.LocationCell;
import org.bukkit.World;

public class SpigotMySQLCustomEndpointManager extends MySQLCustomEndpointManager<LocationCell, World> {

  public SpigotMySQLCustomEndpointManager() {
    super(new SpigotDataConverter());
  }

}
