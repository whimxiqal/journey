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

package net.whimxiqal.journey.navigation;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.chunk.BlockProvider;
import org.jetbrains.annotations.NotNull;

/**
 * A general mode of transportation which determines whether certain locations can be reached by
 * a regular humanoid entity.
 */
public abstract class Mode {

  /**
   * Collect and return all the destinations that are reachable from an original location
   * based on the implementation of this mode.
   * The returned value stores all possible movement options.
   *
   * @param origin the original (current) location
   * @return all options
   * @throws ExecutionException   if the async retrieval of a block had an error
   * @throws InterruptedException if the async retrieval of a block was interrupted
   */
  public abstract Collection<Option> getDestinations(Cell origin, BlockProvider blockProvider) throws ExecutionException, InterruptedException;

  /**
   * Get the mode type.
   *
   * @return the mode type
   */
  @NotNull
  public abstract ModeType type();

  /**
   * A record to store a movement option. It just contains a location and a distance to that location.
   */
  public record Option(Cell location, double cost) {

    /**
     * General constructor.
     *
     * @param location the location
     * @param cost     the destination
     */
    public Option(@NotNull Cell location, double cost) {
      this.location = location;
      this.cost = cost;
    }

    public static Option between(Cell origin, int destinationX, int destinationY, int destinationZ) {
      Cell destination = new Cell(destinationX, destinationY, destinationZ, origin.domain());
      return new Option(destination, origin.distanceTo(destination));
    }

    public static Option between(Cell origin, Cell destination, double costMultiplier) {
      return new Option(destination, origin.distanceTo(destination));
    }

    /**
     * Get location.
     *
     * @return the location
     */
    @Override
    @NotNull
    public Cell location() {
      return location;
    }

    /**
     * Get the distance it would take to reach the location.
     *
     * @return the distance
     */
    @Override
    public double cost() {
      return cost;
    }
  }

}
