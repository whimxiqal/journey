package edu.whimc.journey.spigot.navigation.mode;

import edu.whimc.journey.common.navigation.ModeType;
import edu.whimc.journey.spigot.navigation.LocationCell;
import java.util.Set;
import org.bukkit.Material;

public class SwimMode extends SpigotMode {

  public SwimMode(Set<Material> forcePassable) {
    super(forcePassable);
  }

  @Override
  public void collectDestinations(LocationCell origin) {
    // TODO implement
  }

  @Override
  public ModeType getType() {
    return ModeType.SWIM;
  }
}
