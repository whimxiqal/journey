package edu.whimc.indicator.destination;

import edu.whimc.indicator.api.destination.Destination;
import edu.whimc.indicator.search.LocationLocatable;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class IndicatorDestination extends Destination<JavaPlugin, LocationLocatable, World> {
  public IndicatorDestination(JavaPlugin purpose, LocationLocatable location) {
    super(purpose, location);
  }
}
