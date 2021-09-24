package edu.whimc.indicator.spigot.navigation.mode;

import edu.whimc.indicator.common.navigation.Mode;
import edu.whimc.indicator.common.navigation.ModeType;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import org.bukkit.World;

public class SwimMode extends Mode<LocationCell, World> {
  @Override
  public void collectDestinations(LocationCell origin) {
    // TODO implement
  }

  @Override
  public ModeType getType() {
    return ModeType.SWIM;
  }
}
