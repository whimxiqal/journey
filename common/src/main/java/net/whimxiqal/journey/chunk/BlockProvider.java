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

package net.whimxiqal.journey.chunk;

import java.util.concurrent.ExecutionException;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.proxy.JourneyBlock;

/**
 * A provider that retrieves {@link JourneyBlock}s from the server engine given a
 * geometric grid {@link Cell}.
 */
public interface BlockProvider {

  /**
   * The height of the space filled with air to be considered the surface of the world.
   */
  int AT_SURFACE_HEIGHT = 64;

  static boolean isAtSurface(BlockProvider blockProvider, Cell cell) throws ExecutionException, InterruptedException {
    for (int y = cell.blockY() + 1; y <= Math.min(256, cell.blockY() + AT_SURFACE_HEIGHT); y++) {
      if (!blockProvider.toBlock(new Cell(cell.blockX(), y, cell.blockZ(), cell.domain())).isAir()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Convert a {@link JourneyBlock} into a {@link Cell} asynchronously by querying the server engine
   * in a thread-safe manner.
   *
   * @param cell the cell
   * @return the block
   * @throws ExecutionException   if an error occurred during the async operation to get the block
   * @throws InterruptedException if the async operation to get the block was interrupted
   */
  JourneyBlock toBlock(Cell cell) throws ExecutionException, InterruptedException;

}
