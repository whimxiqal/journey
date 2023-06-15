package net.whimxiqal.journey.search;

import net.whimxiqal.journey.Cell;

/**
 * A step along a path.
 */
public interface SearchStep {

  /**
   * The location that the step arrives at.
   *
   * @return the location
   */
  Cell location();

  /**
   * The mode used to perform the step.
   *
   * @return the mode
   */
  ModeType mode();

}
