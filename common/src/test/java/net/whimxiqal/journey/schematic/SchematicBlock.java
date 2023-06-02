package net.whimxiqal.journey.schematic;

import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Optional;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.proxy.JourneyBlock;
import net.whimxiqal.journey.proxy.JourneyDoor;

public record SchematicBlock(Cell cell, BlockType block) implements JourneyBlock {

  @Override
  public Cell cell() {
    return cell;
  }

  @Override
  public boolean isAir() {
    return block.equals(BlockTypes.AIR);
  }

  @Override
  public boolean isNetherPortal() {
    return block.equals(BlockTypes.NETHER_PORTAL);
  }

  @Override
  public boolean isWater() {
    return block.equals(BlockTypes.WATER);
  }

  @Override
  public boolean isPressurePlate() {
    return false;
  }

  @Override
  public boolean isClimbable() {
    return block.equals(BlockTypes.LADDER);
  }

  @Override
  public boolean isPassable() {
    return switch (block.getId()) {
      case "minecraft:air", "minecraft:grass", "minecraft:tall_grass" -> true;
      default -> false;
    };
  }

  @Override
  public boolean isLaterallyPassable() {
    return isPassable();
  }

  @Override
  public boolean isVerticallyPassable() {
    return isPassable();
  }

  @Override
  public boolean canStandOn() {
    return !isPassable();
  }

  @Override
  public boolean canStandIn() {
    return false;
  }

  @Override
  public float hardness() {
    return 1;
  }

  @Override
  public double height() {
    return isPassable() ? 0 : 1;
  }

  @Override
  public Optional<JourneyDoor> asDoor() {
    return Optional.empty();
  }

  @Override
  public String toString() {
    return "SchematicBlock (" + block.getId() + ") @ " + cell;
  }
}
