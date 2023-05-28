package net.whimxiqal.journey.bukkit.chunk;

import net.whimxiqal.journey.chunk.Direction;
import net.whimxiqal.journey.proxy.JourneyDoor;
import org.bukkit.Material;
import org.bukkit.block.data.type.Door;

public record BukkitJourneyDoor(Door door) implements JourneyDoor {

  @Override
  public Direction direction() {
    return switch (door.getFacing()) {
      case EAST -> Direction.POSITIVE_X;
      case WEST -> Direction.NEGATIVE_X;
      case UP -> Direction.POSITIVE_Y;
      case DOWN -> Direction.NEGATIVE_Y;
      case SOUTH -> Direction.POSITIVE_Z;
      case NORTH -> Direction.NEGATIVE_Z;
      default -> throw new IllegalStateException("Unexpected value: " + door.getFacing());
    };
  }

  @Override
  public boolean isOpen() {
    return door.isOpen();
  }

  @Override
  public boolean isIron() {
    return door.getMaterial() == Material.IRON_DOOR;
  }
}
