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

package net.whimxiqal.journey;

/**
 * A connection from one location to another, indicating the ability for a player to travel through
 * unnatural methods like by typing commands to teleport.
 */
public interface Tunnel extends Permissible {

  int DEFAULT_COST = 1;

  static TunnelBuilder builder(Cell origin, Cell destination) {
    return new TunnelBuilder(origin, destination);
  }

  /**
   * The starting location, or "entrance" to the tunnel.
   *
   * @return the origin
   */
  Cell origin();

  /**
   * The ending location, or "exit" of the tunnel.
   *
   * @return the destination
   */
  Cell destination();

  /**
   * The cost of traveling along the tunnel. A cost of 1 indicates the cost to walk a single block.
   * This may not be negative.
   *
   * @return the cost
   */
  default int cost() {
    return DEFAULT_COST;
  }

  /**
   * The prompt to signal that the tunnel should be traversed.
   * This is useful when the player needs guidance on how to enter the tunnel,
   * like which command they need to type.
   */
  default void prompt() {
    // nothing
  }

  /**
   * Whether the given location is one which constitutes the tunnel being traversed.
   * By default, the tunnel is completed if the distance from the destination is at most 1.
   *
   * @param location the location
   * @return true if the tunnel would be completed with the given location
   */
  default boolean testCompletion(Cell location) {
    return location.distanceToSquared(destination()) <= 1;
  }

}
