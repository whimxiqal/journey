package edu.whimc.indicator.spigot.util;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;

public final class SpigotUtil {

  public static final double STEVE_HEIGHT = 1.8;
  public static final double STEVE_HEIGHT_SNEAK = 1.5;
  public static final double STEVE_HEIGHT_GLIDE = 0.6;
  public static final double STEVE_HEIGHT_SWIM = 0.6;
  public static final double STEVE_WIDTH = 0.6;

  public static final double STEP_HEIGHT = 0.5;

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
    return MaterialGroups.VERTICALLY_PASSABLE.contains(block.getBlockData());
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
    return MaterialGroups.LATERALLY_PASSABLE.contains(block.getBlockData());
  }

  /**
   * Return true if you can pass through this block in any direction.
   *
   * @param block the block
   * @return false if you cannot pass through at all
   */
  public static boolean isPassable(Block block) {
    return block.isPassable() && !MaterialGroups.INVALID_PASSABLE.contains(block.getBlockData());
  }

  /**
   * Can a player be supported by this block below him
   *
   * @param block the block
   * @return false if a player cannot stand on top of the block
   */
  public static boolean canStandOn(Block block) {
    return (!block.isPassable()/* && block.getBoundingBox().getHeight() >= 1.0*/)
        || MaterialGroups.TALL_SOLIDS.contains(block.getBlockData());
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

  public static Slab setSlabType(BlockData blockData, Slab.Type type) {
    if (blockData instanceof Slab) {
      Slab slab = ((Slab) blockData);
      slab.setType(type);
      return slab;
    } else {
      throw new IllegalArgumentException("You may only pass Slab block data!");
    }
  }

}