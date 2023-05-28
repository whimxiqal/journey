package net.whimxiqal.journey.proxy;

import java.util.Optional;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.platform.CellType;
import net.whimxiqal.journey.platform.TestPlatformProxy;
import net.whimxiqal.journey.platform.TestWorld;

public record TestJourneyBlock(Cell cell) implements JourneyBlock {

  @Override
  public boolean isNetherPortal() {
    return false;
  }

  @Override
  public boolean isWater() {
    return false;
  }

  @Override
  public boolean isPressurePlate() {
    return false;
  }

  @Override
  public boolean isClimbable() {
    return false;
  }

  @Override
  public boolean isPassable() {
    return !isBarrier();
  }

  @Override
  public boolean isLaterallyPassable() {
    return !isBarrier();
  }

  @Override
  public boolean isVerticallyPassable() {
    return !isBarrier();
  }

  private boolean isBarrier() {
    if (cell.blockY() < 0 || cell.blockY() > 1) {
      // We cannot go up and down, we are stuck in the horizontal plane
      return true;
    }
    TestWorld world = TestPlatformProxy.worlds.get(cell.domain());
    assert world != null;
    int flatWorldX = cell.blockX();
    int flatWorldY = cell.blockZ();
    if (flatWorldX < 0 || flatWorldX >= world.lengthX || flatWorldY < 0 || flatWorldY >= world.lengthY) {
      return true;  // this is past the border -- we can't go here
    }
    return world.cells[flatWorldY][flatWorldX] == CellType.BARRIER;  // this is a barrier cell -- we can't go here
  }

  @Override
  public boolean canStandOn() {
    return cell.blockY() == -1;
  }

  @Override
  public boolean canStandIn() {
    return !isBarrier();
  }

  @Override
  public float hardness() {
    return 1;
  }

  @Override
  public double height() {
    return 1;
  }

  @Override
  public Optional<JourneyDoor> asDoor() {
    return Optional.empty();
  }
}
