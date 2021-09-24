package edu.whimc.indicator.spigot.data.sql.sqlite;

import edu.whimc.indicator.common.data.sql.sqlite.SQLiteServerEndpointManager;
import edu.whimc.indicator.spigot.data.SpigotDataConverter;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import org.bukkit.World;

public class SpigotSQLiteServerEndpointManager extends SQLiteServerEndpointManager<LocationCell, World> {
  public SpigotSQLiteServerEndpointManager() {
    super(new SpigotDataConverter());
  }
}
