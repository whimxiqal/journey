package net.whimxiqal.journey.bukkit.chunk;

import java.util.Optional;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import net.whimxiqal.journey.bukkit.util.MaterialGroups;
import net.whimxiqal.journey.proxy.JourneyBlock;
import net.whimxiqal.journey.proxy.JourneyDoor;
import net.whimxiqal.journey.search.flag.FlagSet;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;

public record BukkitSessionJourneyBlock(Cell cell,
                                        BlockData data,
                                        FlagSet flagSet) implements JourneyBlock {

  @Override
  public boolean isNetherPortal() {
    return data.getMaterial() == Material.NETHER_PORTAL;
  }

  @Override
  public boolean isWater() {
    return data.getMaterial() == Material.WATER;
  }

  @Override
  public boolean isPressurePlate() {
    return MaterialGroups.PRESSURE_PLATES.contains(data.getMaterial());
  }

  @Override
  public boolean isClimbable() {
    return MaterialGroups.isClimbable(data.getMaterial());
  }

  @Override
  public boolean isPassable() {
    return BukkitUtil.isPassable(data);
  }

  @Override
  public boolean isLaterallyPassable() {
    return BukkitUtil.isLaterallyPassable(data, flagSet);
  }

  @Override
  public boolean isVerticallyPassable() {
    return BukkitUtil.isVerticallyPassable(data);
  }

  @Override
  public boolean canStandOn() {
    return BukkitUtil.canStandOn(data);
  }

  @Override
  public boolean canStandIn() {
    return BukkitUtil.canStandIn(data);
  }

  @Override
  public float hardness() {
    return data.getMaterial().getHardness();
  }

  @Override
  public double height() {
    return MaterialGroups.height(data.getMaterial());
  }

  @Override
  public Optional<JourneyDoor> asDoor() {
    if (data instanceof Door) {
      return Optional.of(new BukkitJourneyDoor((Door) data));
    }
    return Optional.empty();
  }


}
