package edu.whimc.indicator.spigot.util;

import edu.whimc.indicator.Indicator;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;

public final class SpigotUtil {

  /**
   * Return true if you can pass vertically through this block.
   *
   * @param block the block
   * @return false if you cannot pass through this block vertically
   */
  public static boolean isVerticallyPassable(Block block) {
    if (isPassable(block)) {
      return true;
    }
    if (MaterialGroups.VERTICALLY_PASSABLE.contains(block.getType())) {
      return true;
    }
    return false;
  }

  /**
   * Return true if you can pass laterally through this block.
   *
   * @param block the block
   * @return false if you cannot pass through this block laterally
   */
  public static boolean isLaterallyPassable(Block block) {
    if (isPassable(block)) {
      return true;
    }
    if (MaterialGroups.LATERALLY_PASSABLE.contains(block.getType())) {
      if (block.getBlockData() instanceof Slab) {
        return ((Slab) block.getBlockData()).getType().equals(Slab.Type.BOTTOM);
      }
      return true;
    }
    return false;
  }

  /**
   * Return true if you can pass through this block in any direction.
   *
   * @param block the block
   * @return false if you cannot pass through at all
   */
  public static boolean isPassable(Block block) {
    return block.isPassable() && !MaterialGroups.INVALID_PASSABLE.contains(block.getType());
  }

  /**
   * Can a player be supported by this block below him
   *
   * @param block the block
   * @return false if a player cannot stand on top of the block
   */
  public static boolean canStandOn(Block block) {
    return (!block.isPassable() && block.getBoundingBox().getHeight() >= 1.0)
        || MaterialGroups.TALL_SOLIDS.contains(block.getType());
  }

  /**
   * Can a player be supported by this block within the same block as him
   *
   * @param block the block
   * @return false if a player cannot stand within this block and be supported
   */
  public static boolean canStandIn(Block block) {
    return isLaterallyPassable(block)
        && !isVerticallyPassable(block);
  }

}
