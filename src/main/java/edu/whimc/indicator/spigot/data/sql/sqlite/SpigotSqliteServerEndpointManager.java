package edu.whimc.indicator.spigot.data.sql.sqlite;

import edu.whimc.indicator.common.data.sql.sqlite.SqliteServerEndpointManager;
import edu.whimc.indicator.spigot.data.SpigotDataConverter;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import org.bukkit.World;

public class SpigotSqliteServerEndpointManager extends SqliteServerEndpointManager<LocationCell, World> {
  public SpigotSqliteServerEndpointManager(String address) {
    super(address, new SpigotDataConverter());
  }
}
