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
import net.whimxiqal.journey.search.ModeType;
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
   * A record to store a movement option. It just contains a location for now, but it could later
   * contain information about the speed at which the distance to the location can be traversed.
   */
  public record Option(Cell location) {
  }

}
