package edu.whimc.indicator.search.mode;

import edu.whimc.indicator.api.search.Mode;
import edu.whimc.indicator.path.SpigotLocatable;
import org.bukkit.World;

import java.util.Set;

public class FlyMode implements Mode<SpigotLocatable, World> {
  @Override
  public Set<SpigotLocatable> getDestinations(SpigotLocatable origin) {
    // TODO implement
    return null;
  }
}
