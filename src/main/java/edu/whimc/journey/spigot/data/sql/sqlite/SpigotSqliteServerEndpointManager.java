package edu.whimc.journey.spigot.data.sql.sqlite;

import edu.whimc.journey.common.data.sql.sqlite.SqliteServerEndpointManager;
import edu.whimc.journey.spigot.data.SpigotDataConverter;
import edu.whimc.journey.spigot.navigation.LocationCell;
import org.bukkit.World;

public class SpigotSqliteServerEndpointManager extends SqliteServerEndpointManager<LocationCell, World> {
  public SpigotSqliteServerEndpointManager(String address) {
    super(address, new SpigotDataConverter());
  }
}
