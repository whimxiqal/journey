/*
 * MIT License
 *
 * Copyright (c) whimxiqal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.whimxiqal.journey.sponge.util;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;

/**
 * A utility class to enumerate out groups of materials for classification purposes.
 */
public final class MaterialGroups {

  private MaterialGroups() {
  }

  public static boolean isVerticallySpecialPassable(BlockType type) {
    return type.equals(BlockTypes.LADDER.get());
  }

  /**
   * Anything that a player can squeeze through laterally, other than the obvious.
   */
  public static boolean isLaterallySpecialPassable(BlockType type) {
    return type.equals(BlockTypes.WHITE_CARPET.get())
        || type.equals(BlockTypes.ORANGE_CARPET.get())
        || type.equals(BlockTypes.MAGENTA_CARPET.get())
        || type.equals(BlockTypes.LIGHT_BLUE_CARPET.get())
        || type.equals(BlockTypes.YELLOW_CARPET.get())
        || type.equals(BlockTypes.LIME_CARPET.get())
        || type.equals(BlockTypes.PINK_CARPET.get())
        || type.equals(BlockTypes.GRAY_CARPET.get())
        || type.equals(BlockTypes.LIGHT_GRAY_CARPET.get())
        || type.equals(BlockTypes.CYAN_CARPET.get())
        || type.equals(BlockTypes.PURPLE_CARPET.get())
        || type.equals(BlockTypes.BLUE_CARPET.get())
        || type.equals(BlockTypes.BROWN_CARPET.get())
        || type.equals(BlockTypes.GREEN_CARPET.get())
        || type.equals(BlockTypes.RED_CARPET.get())
        || type.equals(BlockTypes.BLACK_CARPET.get());
  }

  public static double height(BlockType type) {
    if (type.equals(BlockTypes.AIR.get())
        || type.equals(BlockTypes.CAVE_AIR)) {
      return 0;
    }
    return 1;
  }

  public static boolean isTwoBlocksTall(BlockType type) {
    return type.equals(BlockTypes.ACACIA_FENCE.get())
        || type.equals(BlockTypes.BIRCH_FENCE.get())
        || type.equals(BlockTypes.CRIMSON_FENCE.get())
        || type.equals(BlockTypes.DARK_OAK_FENCE.get())
        || type.equals(BlockTypes.JUNGLE_FENCE.get())
        || type.equals(BlockTypes.OAK_FENCE.get())
        || type.equals(BlockTypes.NETHER_BRICK_FENCE.get())
        || type.equals(BlockTypes.SPRUCE_FENCE.get())
        || type.equals(BlockTypes.WARPED_FENCE.get())
        || type.equals(BlockTypes.ANDESITE_WALL.get())
        || type.equals(BlockTypes.BLACKSTONE_WALL.get())
        || type.equals(BlockTypes.BRICK_WALL.get())
        || type.equals(BlockTypes.COBBLESTONE_WALL.get())
        || type.equals(BlockTypes.COBBLED_DEEPSLATE_WALL.get())
        || type.equals(BlockTypes.DEEPSLATE_BRICK_WALL.get())
        || type.equals(BlockTypes.DIORITE_WALL.get())
        || type.equals(BlockTypes.DEEPSLATE_TILE_WALL.get())
        || type.equals(BlockTypes.GRANITE_WALL.get())
        || type.equals(BlockTypes.END_STONE_BRICK_WALL.get())
        || type.equals(BlockTypes.MOSSY_COBBLESTONE_WALL.get())
        || type.equals(BlockTypes.MOSSY_STONE_BRICK_WALL.get())
        || type.equals(BlockTypes.NETHER_BRICK_WALL.get())
        || type.equals(BlockTypes.POLISHED_BLACKSTONE_BRICK_WALL.get())
        || type.equals(BlockTypes.POLISHED_BLACKSTONE_WALL.get())
        || type.equals(BlockTypes.PRISMARINE_WALL.get())
        || type.equals(BlockTypes.POLISHED_DEEPSLATE_WALL.get())
        || type.equals(BlockTypes.RED_NETHER_BRICK_WALL.get())
        || type.equals(BlockTypes.RED_SANDSTONE_WALL.get())
        || type.equals(BlockTypes.SANDSTONE_WALL.get())
        || type.equals(BlockTypes.STONE_BRICK_WALL.get());
  }

}
