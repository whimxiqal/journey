package net.whimxiqal.journey.proxy;

import java.util.Optional;
import net.whimxiqal.journey.Cell;

public record AirJourneyBlock(Cell cell) implements JourneyBlock {
  @Override
  public boolean isAir() {
    return true;
  }

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
    return true;
  }

  @Override
  public boolean isLaterallyPassable() {
    return true;
  }

  @Override
  public boolean isVerticallyPassable() {
    return true;
  }

  @Override
  public boolean canStandOn() {
    return false;
  }

  @Override
  public boolean canStandIn() {
    return false;
  }

  @Override
  public float hardness() {
    return 0;
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
