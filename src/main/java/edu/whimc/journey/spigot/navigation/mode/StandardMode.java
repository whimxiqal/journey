//package edu.whimc.indicator.spigot.path.mode;
//
//import edu.whimc.indicator.common.path.Mode;
//import edu.whimc.indicator.spigot.path.LocationCell;
//import edu.whimc.indicator.spigot.util.SpigotUtil;
//import org.bukkit.World;
//import org.bukkit.block.Block;
//import org.jetbrains.annotations.Nullable;
//
///**
// * Standard mode for moving from one block location to an adjacent block location.
// * We assume the player starts in the middle of the block in X and Z direction,
// * and has some offset in the Y direction.
// */
//public abstract class StandardMode implements Mode<LocationCell, World> {
//
//  @Nullable
//  private double canFit(int offsetX, int offsetZ) {
//    Block destinationBlock = origin.getBlockAtOffset(1, 0, 0);
//    LocationCell destination = origin.createLocatableAtOffset(1,
//        destinationBlock.isPassable()
//            ? 0
//            : destinationBlock.getBoundingBox().getMaxY(),
//        0);
//    if (origin.getHeightOffset() + SpigotUtil.STEP_HEIGHT < destination.getHeightOffset()) {
//      // We can't step up onto this block
//      return null;
//    }
//    destinationBlock.getBoundingBox()
//
//  }
//
//}
