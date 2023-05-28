package net.whimxiqal.journey.proxy;

import java.util.Optional;
import net.whimxiqal.journey.Cell;

/**
 * Journey's representation of a Minecraft block.
 *
 * TODO: the logic to determine which movement is allowed for an entity should be improved
 */
public interface JourneyBlock {

  Cell cell();

  boolean isNetherPortal();

  boolean isWater();

  boolean isPressurePlate();

  boolean isClimbable();

  /**
   * Whether this can be passed through in any direction.
   * Examples: air, tall grass
   *
   * @return true if it can be passed
   */
  boolean isPassable();

  /**
   * Whether this can be laterally passed through, as in,
   * can an entity move unhindered in a lateral direction through the location.
   * Examples: air, tall grass, and carpets
   *
   * @return true if it can be passed
   */
  boolean isLaterallyPassable();

  /**
   * Whether this can be passed through vertically,
   * like by falling through it or flying upwards through it.
   * Examples: air, ladders
   *
   * @return true if it can be passed
   */
  boolean isVerticallyPassable();

  boolean canStandOn();

  boolean canStandIn();

  float hardness();

  double height();

  Optional<JourneyDoor> asDoor();

}
