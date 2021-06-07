package edu.whimc.indicator.spigot.path.mode;

import edu.whimc.indicator.common.path.Mode;
import edu.whimc.indicator.common.path.ModeType;
import edu.whimc.indicator.common.path.ModeTypes;
import edu.whimc.indicator.spigot.path.LocationCell;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;

public class ClimbMode implements Mode<LocationCell, World> {
  @Override
  public Map<LocationCell, Double> getDestinations(LocationCell origin) {
    Map<LocationCell, Double> ladderBlocks = new HashMap<>();

    if (canClimb(origin.getBlockAtOffset(1, 0, 0))) {
      ladderBlocks.put(origin.createLocatableAtOffset(1, 0, 0), 1.0d);
    }
    if (canClimb(origin.getBlockAtOffset(-1, 0, 0))) {
      ladderBlocks.put(origin.createLocatableAtOffset(-1, 0, 0), 1.0d);
    }
    if (canClimb(origin.getBlockAtOffset(0, 1, 0))) {
      ladderBlocks.put(origin.createLocatableAtOffset(0, 1, 0), 1.0d);
    }
    if (canClimb(origin.getBlockAtOffset(0, -1, 0))) {
      ladderBlocks.put(origin.createLocatableAtOffset(0, -1, 0), 1.0d);
    }
    if (canClimb(origin.getBlockAtOffset(0, 0, 1))) {
      ladderBlocks.put(origin.createLocatableAtOffset(0, 0, 1), 1.0d);
    }
    if (canClimb(origin.getBlockAtOffset(0, 0, -1))) {
      ladderBlocks.put(origin.createLocatableAtOffset(0, 0, -1), 1.0d);
    }

    return ladderBlocks;
  }

  private boolean canClimb(Block block) {
    if (block.getType().equals(Material.LADDER)) {
      return true;
    }
    if (block.getType().equals(Material.VINE)) {
      // Need to make sure the backside has a solid block
      // TODO implement
    }
    return false;
  }

  @Override
  public ModeType getType() {
    return ModeTypes.CLIMB;
  }
}
