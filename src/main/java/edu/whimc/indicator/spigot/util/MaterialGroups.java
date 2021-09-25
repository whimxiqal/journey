package edu.whimc.indicator.spigot.util;

import com.google.common.collect.Sets;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public final class MaterialGroups {

  /**
   * All pressure plates.
   */
  public static final Set<BlockData> PRESSURE_PLATES = Sets.newHashSet(
      Material.ACACIA_PRESSURE_PLATE.createBlockData(),
      Material.BIRCH_PRESSURE_PLATE.createBlockData(),
      Material.CRIMSON_PRESSURE_PLATE.createBlockData(),
      Material.DARK_OAK_PRESSURE_PLATE.createBlockData(),
      Material.HEAVY_WEIGHTED_PRESSURE_PLATE.createBlockData(),
      Material.JUNGLE_PRESSURE_PLATE.createBlockData(),
      Material.LIGHT_WEIGHTED_PRESSURE_PLATE.createBlockData(),
      Material.OAK_PRESSURE_PLATE.createBlockData(),
      Material.POLISHED_BLACKSTONE_PRESSURE_PLATE.createBlockData(),
      Material.SPRUCE_PRESSURE_PLATE.createBlockData(),
      Material.STONE_PRESSURE_PLATE.createBlockData(),
      Material.WARPED_PRESSURE_PLATE.createBlockData()
  );
  /**
   * Anything that a player, under any circumstance, is not allowed to touch.
   */
  public static final Set<BlockData> INVALID_PASSABLE = Sets.newHashSet(
      Material.LAVA.createBlockData(),
      Material.NETHER_PORTAL.createBlockData(),
      Material.END_PORTAL.createBlockData());
  /**
   * Anything that a player can squeeze through vertically, other than the obvious.
   */
  public static final Set<BlockData> VERTICALLY_PASSABLE = Sets.newHashSet(
      Material.LADDER.createBlockData());
  /**
   * Anything that a player can squeeze through laterally, other than the obvious.
   */
  public static final Set<BlockData> LATERALLY_PASSABLE = Sets.newHashSet(
      Material.WHITE_CARPET.createBlockData(),
      Material.ORANGE_CARPET.createBlockData(),
      Material.MAGENTA_CARPET.createBlockData(),
      Material.LIGHT_BLUE_CARPET.createBlockData(),
      Material.YELLOW_CARPET.createBlockData(),
      Material.LIME_CARPET.createBlockData(),
      Material.PINK_CARPET.createBlockData(),
      Material.GRAY_CARPET.createBlockData(),
      Material.LIGHT_GRAY_CARPET.createBlockData(),
      Material.CYAN_CARPET.createBlockData(),
      Material.PURPLE_CARPET.createBlockData(),
      Material.BLUE_CARPET.createBlockData(),
      Material.BROWN_CARPET.createBlockData(),
      Material.GREEN_CARPET.createBlockData(),
      Material.RED_CARPET.createBlockData(),
      Material.BLACK_CARPET.createBlockData()/*,
      SpigotUtil.setSlabType(Material.OAK_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.SPRUCE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.BIRCH_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.JUNGLE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.ACACIA_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.DARK_OAK_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.STONE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.SANDSTONE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.PETRIFIED_OAK_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.COBBLESTONE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.BRICK_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.STONE_BRICK_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.NETHER_BRICK_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.QUARTZ_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.RED_SANDSTONE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.PURPUR_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.PRISMARINE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.PRISMARINE_BRICK_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.DARK_PRISMARINE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.SMOOTH_QUARTZ_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.SMOOTH_STONE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.CUT_SANDSTONE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.CUT_RED_SANDSTONE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.POLISHED_GRANITE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.SMOOTH_RED_SANDSTONE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.MOSSY_STONE_BRICK_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.POLISHED_DIORITE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.MOSSY_COBBLESTONE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.END_STONE_BRICK_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.SMOOTH_SANDSTONE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.SMOOTH_QUARTZ_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.GRANITE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.ANDESITE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.RED_NETHER_BRICK_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.POLISHED_ANDESITE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.DIORITE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.CRIMSON_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.WARPED_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.BLACKSTONE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.POLISHED_BLACKSTONE_SLAB.createBlockData(), Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.POLISHED_BLACKSTONE_BRICK_SLAB.createBlockData(), Slab.Type.BOTTOM)*/);
  /**
   * Anything that a player can stand on, other than the obvious.
   */
  public static final Set<BlockData> TALL_SOLIDS = Sets.newHashSet(
      Material.LADDER.createBlockData());

  private MaterialGroups() {
  }

}
