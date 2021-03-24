package edu.whimc.indicator.spigot.path.mode;

import edu.whimc.indicator.common.path.Mode;
import edu.whimc.indicator.common.path.ModeType;
import edu.whimc.indicator.common.path.ModeTypes;
import edu.whimc.indicator.spigot.path.LocationCell;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class SwimMode implements Mode<LocationCell, World> {
  @Override
  public Map<LocationCell, Double> getDestinations(LocationCell origin) {
    // TODO implement
    return new HashMap<>();
  }

  @Override
  public ModeType getType() {
    return ModeTypes.SWIM;
  }
}
