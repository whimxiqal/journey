package net.whimxiqal.journey.chunk;

/**
 * A Minecraft object that has an associated direction.
 * Examples: doors
 */
public interface Directional {

  /**
   * The direction this is facing.
   *
   * @return the direction
   */
  Direction direction();

}
