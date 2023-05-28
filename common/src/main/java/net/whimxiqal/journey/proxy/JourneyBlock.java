/*
 * MIT License
 *
 * Copyright (c) whimxiqal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.whimxiqal.journey.proxy;

import java.util.Optional;
import net.whimxiqal.journey.Cell;

/**
 * Journey's representation of a Minecraft block.
 * Read-only and thread-safe.
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
