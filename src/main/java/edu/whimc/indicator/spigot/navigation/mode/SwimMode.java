package edu.whimc.indicator.spigot.navigation.mode;

import edu.whimc.indicator.common.navigation.Mode;
import edu.whimc.indicator.common.navigation.ModeType;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

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
