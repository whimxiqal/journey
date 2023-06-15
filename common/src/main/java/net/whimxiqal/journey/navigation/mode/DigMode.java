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
import net.whimxiqal.journey.search.ModeType;
import org.jetbrains.annotations.NotNull;

public class DigMode extends Mode {

  @Override
  public Collection<Option> getDestinations(Cell origin, BlockProvider blockProvider) throws ExecutionException, InterruptedException {
    List<Option> options = new LinkedList<>();
    // Can we even stand here?
    if (!blockProvider.toBlock(origin.atOffset(0, -1, 0)).canStandOn()
        && !blockProvider.toBlock(origin.atOffset(0, 0, 0)).canStandIn()) {
      return options;
    }
    Cell cell;
    // Check every block in a 3x3 grid centered around the current location
    for (int offX = -1; offX <= 1; offX++) {
      for (int offY = -1; offY <= 1; offY++) {
        outerZ:
        // Label so we can continue from these main loops when all checks fail
        for (int offZ = -1; offZ <= 1; offZ++) {
          if (offX == 0 && offY == 1 && offZ == 0) {
            // we cannot dig straight up
            continue;
          }
          // Checks for the block -- checks between the offset block and the original block,
          //  which would be 1 for just 1 offset variable, 4 for 2 offset variables,
          //  and 8 for 3 offset variables.
          for (int insideOffX = offX * offX /* normalize sign */; insideOffX >= 0; insideOffX--) {
            for (int insideOffY = offY * offY /* normalize sign */; insideOffY >= 0; insideOffY--) {
              for (int insideOffZ = offZ * offZ /* normalize sign */; insideOffZ >= 0; insideOffZ--) {
                // This is the origin, we don't want to move here
                if (insideOffX == 0 && insideOffY == 0 && insideOffZ == 0) {
                  continue;
                }
                // Make sure we get the pillar of y values for the player's body
                cell = origin.atOffset(// Floor
                    insideOffX * offX /* get sign back */,
                    insideOffY * offY /* get sign back */,
                    insideOffZ * offZ /* get sign back */);
                if (!blockProvider.toBlock(cell).isLaterallyPassable()) {
                  // we must break it
                  if (blockProvider.toBlock(cell).hardness() < 0) {
                    continue outerZ;
                  }
                }
                for (int h = 0; h <= insideOffY; h++) {
                  // The rest of the pillar above the floor
                  cell = origin.atOffset(
                      insideOffX * offX /* get sign back */,
                      ((insideOffY * offY + insideOffY) >> 1) /* 1 for positive, 0 for negative */
                          + h
                          + (1 - insideOffY) /* for if offYIn is 0 */,
                      insideOffZ * offZ /* get sign back */);
                  if (!blockProvider.toBlock(cell).isPassable()) {
                    if (blockProvider.toBlock(cell).hardness() < 0) {
                      continue outerZ;
                    }
                  }
                }
              }
            }
          }
          Cell other = origin.atOffset(offX, offY, offZ);
          options.add(new Option(other));
        }
      }
    }
    return options;
  }

  @Override
  public @NotNull ModeType type() {
    return ModeType.DIG;
  }
}
