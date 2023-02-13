/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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

package net.whimxiqal.journey.bukkit.util;

import com.google.common.collect.Sets;
import java.util.Set;
import org.bukkit.Material;

/**
 * A utility class to enumerate out groups of materials for classification purposes.
 */
public final class MaterialGroups {

  /**
   * All pressure plates.
   */
  public static final Set<Material> PRESSURE_PLATES = Sets.newHashSet(
      Material.ACACIA_PRESSURE_PLATE,
      Material.BIRCH_PRESSURE_PLATE,
      Material.CRIMSON_PRESSURE_PLATE,
      Material.DARK_OAK_PRESSURE_PLATE,
      Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
      Material.JUNGLE_PRESSURE_PLATE,
      Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
      Material.OAK_PRESSURE_PLATE,
      Material.POLISHED_BLACKSTONE_PRESSURE_PLATE,
      Material.SPRUCE_PRESSURE_PLATE,
      Material.STONE_PRESSURE_PLATE,
      Material.WARPED_PRESSURE_PLATE
  );
  public static boolean isPassable(Material material) {
    switch (material) {
    //<editor-fold defaultstate="collapsed" desc="isPassable">
      case ACACIA_PRESSURE_PLATE:
      case ACACIA_SIGN:
      case ACACIA_WALL_SIGN:
      case ACTIVATOR_RAIL:
      case AIR:
      case ALLIUM:
      case AZURE_BLUET:
      case BAMBOO:
      case BEETROOTS:
      case BIRCH_PRESSURE_PLATE:
      case BIRCH_SIGN:
      case BIRCH_WALL_SIGN:
      case BLACK_BANNER:
      case BLACK_WALL_BANNER:
      case BLUE_BANNER:
      case BLUE_ORCHID:
      case BLUE_WALL_BANNER:
      case BROWN_BANNER:
      case BROWN_MUSHROOM:
      case BROWN_WALL_BANNER:
      case CARROTS:
      case CAVE_AIR:
      case CAVE_VINES:
      case CHORUS_FLOWER:
      case COCOA_BEANS:
      case CORNFLOWER:
      case CRIMSON_FUNGUS:
      case CRIMSON_PRESSURE_PLATE:
      case CRIMSON_ROOTS:
      case CRIMSON_SIGN:
      case CRIMSON_WALL_SIGN:
      case CYAN_BANNER:
      case CYAN_WALL_BANNER:
      case DANDELION:
      case DARK_OAK_PRESSURE_PLATE:
      case DARK_OAK_SIGN:
      case DARK_OAK_WALL_SIGN:
      case DEAD_BRAIN_CORAL:
      case DEAD_BRAIN_CORAL_FAN:
      case DEAD_BRAIN_CORAL_WALL_FAN:
      case DEAD_BUBBLE_CORAL:
      case DEAD_BUBBLE_CORAL_FAN:
      case DEAD_BUBBLE_CORAL_WALL_FAN:
      case DEAD_BUSH:
      case DEAD_FIRE_CORAL:
      case DEAD_FIRE_CORAL_FAN:
      case DEAD_FIRE_CORAL_WALL_FAN:
      case DEAD_HORN_CORAL:
      case DEAD_HORN_CORAL_FAN:
      case DEAD_HORN_CORAL_WALL_FAN:
      case DEAD_TUBE_CORAL:
      case DEAD_TUBE_CORAL_FAN:
      case DEAD_TUBE_CORAL_WALL_FAN:
      case DETECTOR_RAIL:
      case FIRE:
      case GLOW_BERRIES:
      case GLOW_LICHEN:
      case GRASS:
      case GRAY_BANNER:
      case GRAY_WALL_BANNER:
      case GREEN_BANNER:
      case GREEN_WALL_BANNER:
      case HANGING_ROOTS:
      case HEAVY_WEIGHTED_PRESSURE_PLATE:
      case JUNGLE_PRESSURE_PLATE:
      case JUNGLE_SIGN:
      case JUNGLE_WALL_SIGN:
      case KELP:
      case LEVER:
      case LIGHT_BLUE_BANNER:
      case LIGHT_BLUE_WALL_BANNER:
      case LIGHT_GRAY_BANNER:
      case LIGHT_GRAY_WALL_BANNER:
      case LIGHT_WEIGHTED_PRESSURE_PLATE:
      case LILAC:
      case LILY_OF_THE_VALLEY:
      case LIME_BANNER:
      case LIME_WALL_BANNER:
      case MAGENTA_BANNER:
      case MAGENTA_WALL_BANNER:
      case MELON_SEEDS:
      case MOSS_CARPET:
      case NETHER_PORTAL:
      case NETHER_SPROUTS:
      case NETHER_WART:
      case OAK_PRESSURE_PLATE:
      case OAK_SIGN:
      case OAK_WALL_SIGN:
      case ORANGE_BANNER:
      case ORANGE_TULIP:
      case ORANGE_WALL_BANNER:
      case OXEYE_DAISY:
      case PEONY:
      case PINK_BANNER:
      case PINK_TULIP:
      case PINK_WALL_BANNER:
      case POLISHED_BLACKSTONE_PRESSURE_PLATE:
      case POPPY:
      case POTATOES:
      case POTTED_CRIMSON_FUNGUS:
      case POTTED_ORANGE_TULIP:
      case POTTED_PINK_TULIP:
      case POTTED_RED_TULIP:
      case POTTED_WHITE_TULIP:
      case POWERED_RAIL:
      case PURPLE_BANNER:
      case PURPLE_WALL_BANNER:
      case RAIL:
      case REDSTONE_TORCH:
      case REDSTONE_WALL_TORCH:
      case REDSTONE_WIRE:
      case RED_BANNER:
      case RED_MUSHROOM:
      case RED_TULIP:
      case RED_WALL_BANNER:
      case ROSE_BUSH:
      case SEAGRASS:
      case SEA_PICKLE:
      case SMALL_DRIPLEAF:
      case SPORE_BLOSSOM:
      case SPRUCE_PRESSURE_PLATE:
      case SPRUCE_SIGN:
      case SPRUCE_WALL_SIGN:
      case STONE_PRESSURE_PLATE:
      case SUGAR_CANE:
      case SUNFLOWER:
      case SWEET_BERRIES:
      case TALL_GRASS:
      case TORCH:
      case TRIPWIRE:
      case TRIPWIRE_HOOK:
      case TWISTING_VINES:
      case WARPED_FUNGUS:
      case WARPED_PRESSURE_PLATE:
      case WARPED_ROOTS:
      case WARPED_SIGN:
      case WARPED_WALL_SIGN:
      case WEEPING_VINES:
      case WHEAT_SEEDS:
      case WHITE_BANNER:
      case WHITE_TULIP:
      case WHITE_WALL_BANNER:
      case WITHER_ROSE:
      case YELLOW_BANNER:
      case YELLOW_WALL_BANNER:
        //</editor-fold>
        return true;
      default:
        return false;
    }
  }
  /**
   * Anything that a player can stand on, other than the obvious.
   */
  public static final Set<Material> TALL_SOLIDS = Sets.newHashSet(
      Material.LADDER);
  public static final Set<Material> BOATS = Sets.newHashSet(
      Material.ACACIA_BOAT,
      Material.BIRCH_BOAT,
      Material.DARK_OAK_BOAT,
      Material.OAK_BOAT,
      Material.JUNGLE_BOAT,
      Material.SPRUCE_BOAT);

  private MaterialGroups() {
  }

  public static boolean isVerticallySpecialPassable(Material material) {
    switch (material) {
      case LADDER:
        return true;
      default:
        return false;
    }
  }

  /**
   * Anything that a player can squeeze through laterally, other than the obvious.
   */
  public static boolean isLaterallySpecialPassable(Material material) {
    switch (material) {
      case WHITE_CARPET:
      case ORANGE_CARPET:
      case MAGENTA_CARPET:
      case LIGHT_BLUE_CARPET:
      case YELLOW_CARPET:
      case LIME_CARPET:
      case PINK_CARPET:
      case GRAY_CARPET:
      case LIGHT_GRAY_CARPET:
      case CYAN_CARPET:
      case PURPLE_CARPET:
      case BLUE_CARPET:
      case BROWN_CARPET:
      case GREEN_CARPET:
      case RED_CARPET:
      case BLACK_CARPET:
        return true;
      default:
        return false;
    }
  }

  public static double height(Material material) {
    switch (material) {
      case AIR:
      case CAVE_AIR:
        return 0;
      default:
        return 1;
    }
  }

}
