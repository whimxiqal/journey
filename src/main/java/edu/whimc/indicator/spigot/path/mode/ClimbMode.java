package edu.whimc.indicator.spigot.path.mode;

import edu.whimc.indicator.common.path.Mode;
import edu.whimc.indicator.common.path.ModeType;
import edu.whimc.indicator.common.path.ModeTypes;
import edu.whimc.indicator.spigot.path.LocationCell;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class ClimbMode extends Mode<LocationCell, World> {
  @Override
  protected void collectDestinations(LocationCell origin) {
    tryToClimb(origin.getBlockAtOffset(1, 0, 0));
    tryToClimb(origin.getBlockAtOffset(-1, 0, 0));
    tryToClimb(origin.getBlockAtOffset(0, 1, 0));
    tryToClimb(origin.getBlockAtOffset(0, -1, 0));
    tryToClimb(origin.getBlockAtOffset(0, 0, 1));
    tryToClimb(origin.getBlockAtOffset(0, 0, -1));
  }

  private void tryToClimb(Block block) {
    LocationCell cell = new LocationCell(block.getLocation());
    if (canClimb(block)) {
      accept(cell, 1.0d);
    } else {
      reject(cell);
    }
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
