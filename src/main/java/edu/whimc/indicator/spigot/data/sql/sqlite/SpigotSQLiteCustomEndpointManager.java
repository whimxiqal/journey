package edu.whimc.indicator.spigot.data.sql.sqlite;

import edu.whimc.indicator.common.data.sql.sqlite.SQLiteCustomEndpointManager;
import edu.whimc.indicator.spigot.data.SpigotDataConverter;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import org.bukkit.World;

public class SpigotSQLiteCustomEndpointManager extends SQLiteCustomEndpointManager<LocationCell, World> {

  /**
   * General constructor.
   */
  public SpigotSQLiteCustomEndpointManager() {
    super(new SpigotDataConverter());
  }
}
