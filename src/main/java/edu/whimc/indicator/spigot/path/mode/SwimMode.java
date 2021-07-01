package edu.whimc.indicator.spigot.path.mode;

import edu.whimc.indicator.common.path.Mode;
import edu.whimc.indicator.common.path.ModeType;
import edu.whimc.indicator.spigot.path.LocationCell;
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
