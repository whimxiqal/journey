package edu.whimc.indicator.destination;

import edu.whimc.indicator.api.path.Destination;
import edu.whimc.indicator.path.SpigotLocatable;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class IndicatorDestination extends Destination<JavaPlugin, SpigotLocatable, World> {
  public IndicatorDestination(JavaPlugin purpose, SpigotLocatable location) {
    super(purpose, location);
  }
}
