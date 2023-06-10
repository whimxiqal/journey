package net.whimxiqal.journey.proxy;

import java.util.Optional;
import net.whimxiqal.journey.Cell;

public record UnavailableJourneyBlock(Cell cell) implements JourneyBlock {

  @Override
  public boolean isAir() {
    return false;
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
    return false;
  }

  @Override
  public boolean isLaterallyPassable() {
    return false;
  }

  @Override
  public boolean isVerticallyPassable() {
    return false;
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
    return Float.MAX_VALUE;
  }

  @Override
  public double height() {
    return Double.MAX_VALUE;
  }

  @Override
  public Optional<JourneyDoor> asDoor() {
    return Optional.empty();
  }
}
