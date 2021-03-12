package edu.whimc.indicator.spigot.search.mode;

import edu.whimc.indicator.api.path.Mode;
import edu.whimc.indicator.api.path.ModeType;
import edu.whimc.indicator.spigot.path.CellImpl;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class FlyMode implements Mode<CellImpl, World> {
  @Override
  public Map<CellImpl, Float> getDestinations(CellImpl origin) {
    // TODO implement
    return new HashMap<>();
  }

  @Override
  public ModeType getType() {
    return ModeTypes.FLY;
  }
}
