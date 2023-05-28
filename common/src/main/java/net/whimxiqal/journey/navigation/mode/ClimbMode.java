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

package net.whimxiqal.journey.navigation.mode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.chunk.BlockProvider;
import net.whimxiqal.journey.navigation.Mode;
import net.whimxiqal.journey.navigation.ModeType;
import net.whimxiqal.journey.search.SearchSession;
import org.jetbrains.annotations.NotNull;

/**
 * A mode to provide the results to whether a player can climb blocks around them,
 * like ladders or vines.
 */
public final class ClimbMode extends Mode {

  public ClimbMode(@NotNull SearchSession session) {
    super(session);
  }

  @Override
  public Collection<Option> getDestinations(Cell origin, BlockProvider blockProvider) throws ExecutionException, InterruptedException {
    List<Option> options = new LinkedList<>();

    // TODO we have to make sure that the ladders and vines are oriented correctly
    //  and that the vines have a solid block behind it
    tryToClimbAdjacent(origin.atOffset(1, 0, 0), blockProvider, options);
    tryToClimbAdjacent(origin.atOffset(-1, 0, 0), blockProvider, options);
    tryToClimbAdjacent(origin.atOffset(0, 0, 1), blockProvider, options);
    tryToClimbAdjacent(origin.atOffset(0, 0, -1), blockProvider, options);
    tryToClimbAdjacent(origin.atOffset(0, -1, 0), blockProvider, options);

    // Going up is a different story
    if (blockProvider.getBlock(origin).isClimbable()) {
      if (blockProvider.getBlock(origin.atOffset(0, 1, 0)).isVerticallyPassable()
          && blockProvider.getBlock(origin.atOffset(0, 2, 0)).isVerticallyPassable()) {
        accept(origin.atOffset(0, 1, 0), 1.0d, options);
      } else {
        reject(origin.atOffset(0, 1, 0));
      }
    }

    return options;
  }

  private void tryToClimbAdjacent(Cell cell, BlockProvider blockProvider, List<Option> options) throws ExecutionException, InterruptedException {
    if (blockProvider.getBlock(cell).isClimbable()) {
      accept(cell, 1.0d, options);
    } else {
      reject(cell);
    }
  }

  @Override
  public @NotNull ModeType type() {
    return ModeType.CLIMB;
  }
}
