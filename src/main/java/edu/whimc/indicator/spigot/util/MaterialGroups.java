package edu.whimc.indicator.spigot.util;

import com.google.common.collect.Sets;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class MaterialGroups {

  private MaterialGroups() {
  }

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

  /**
   * Anything that a player, under any circumstance, is not allowed to touch.
   */
  public static final Set<Material> INVALID_PASSABLE = Sets.newHashSet(
      Material.LAVA,
      Material.NETHER_PORTAL,
      Material.END_PORTAL);

  /**
   * Anything that a player can squeeze through vertically, other than the obvious.
   */
  public static final Set<Material> VERTICALLY_PASSABLE = Sets.newHashSet(
      Material.LADDER);

  /**
   * Anything that a player can squeeze through laterally, other than the obvious.
   */
  public static final Set<Material> LATERALLY_PASSABLE = Sets.newHashSet(
      Material.WHITE_CARPET,
      Material.ORANGE_CARPET,
      Material.MAGENTA_CARPET,
      Material.LIGHT_BLUE_CARPET,
      Material.YELLOW_CARPET,
      Material.LIME_CARPET,
      Material.PINK_CARPET,
      Material.GRAY_CARPET,
      Material.LIGHT_GRAY_CARPET,
      Material.CYAN_CARPET,
      Material.PURPLE_CARPET,
      Material.BLUE_CARPET,
      Material.BROWN_CARPET,
      Material.GREEN_CARPET,
      Material.RED_CARPET,
      Material.BLACK_CARPET,
      Material.OAK_SLAB,
      Material.SPRUCE_SLAB,
      Material.BIRCH_SLAB,
      Material.JUNGLE_SLAB,
      Material.ACACIA_SLAB,
      Material.DARK_OAK_SLAB,
      Material.STONE_SLAB,
      Material.SANDSTONE_SLAB,
      Material.PETRIFIED_OAK_SLAB,
      Material.COBBLESTONE_SLAB,
      Material.BRICK_SLAB,
      Material.STONE_BRICK_SLAB,
      Material.NETHER_BRICK_SLAB,
      Material.QUARTZ_SLAB,
      Material.RED_SANDSTONE_SLAB,
      Material.PURPUR_SLAB,
      Material.PRISMARINE_SLAB,
      Material.PRISMARINE_BRICK_SLAB,
      Material.DARK_PRISMARINE_SLAB,
      Material.SMOOTH_QUARTZ_SLAB,
      Material.SMOOTH_STONE_SLAB,
      Material.CUT_SANDSTONE_SLAB,
      Material.CUT_RED_SANDSTONE_SLAB,
      Material.POLISHED_GRANITE_SLAB,
      Material.SMOOTH_RED_SANDSTONE_SLAB,
      Material.MOSSY_STONE_BRICK_SLAB,
      Material.POLISHED_DIORITE_SLAB,
      Material.MOSSY_COBBLESTONE_SLAB,
      Material.END_STONE_BRICK_SLAB,
      Material.SMOOTH_SANDSTONE_SLAB,
      Material.SMOOTH_QUARTZ_SLAB,
      Material.GRANITE_SLAB,
      Material.ANDESITE_SLAB,
      Material.RED_NETHER_BRICK_SLAB,
      Material.POLISHED_ANDESITE_SLAB,
      Material.DIORITE_SLAB,
      Material.CRIMSON_SLAB,
      Material.WARPED_SLAB,
      Material.BLACKSTONE_SLAB,
      Material.POLISHED_BLACKSTONE_SLAB,
      Material.POLISHED_BLACKSTONE_BRICK_SLAB);

  /**
   * Anything that a player can stand on, other than the obvious.
   */
  public static final Set<Material> TALL_SOLIDS = Sets.newHashSet(
      Material.LADDER);

}
