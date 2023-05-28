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
 * Implementation of {@link Mode}
 * to check the places a player may jump to in Minecraft.
 */
public class JumpMode extends Mode {

  public JumpMode(@NotNull SearchSession session) {
    super(session);
  }

  @Override
  public Collection<Option> getDestinations(Cell origin, BlockProvider blockProvider) throws ExecutionException, InterruptedException {
    List<Option> options = new LinkedList<>();
    Cell cell;

    cell = origin.atOffset(0, -1, 0);
    if (blockProvider.getBlock(cell).isVerticallyPassable()) {
      // Nothing to jump off of
      reject(cell);
      return options;
    }

    cell = origin.atOffset(0, 2, 0);
    if (!blockProvider.getBlock(cell).isVerticallyPassable()) {
      // No room to jump
      reject(cell);
      return options;
    }
    // 1 block up
    accept(origin.atOffset(0, 1, 0), 1.0d, options);

    // 1 block away and up
    for (int offX = -1; offX <= 1; offX++) {
      outerZ:
      for (int offZ = -1; offZ <= 1; offZ++) {
        for (int insideOffX = offX * offX /* normalize sign */; insideOffX >= 0; insideOffX--) {
          for (int insideOffZ = offZ * offZ /* normalize sign */; insideOffZ >= 0; insideOffZ--) {
            if (insideOffX == 0 && insideOffZ == 0) {
              continue;
            }
            // Check two blocks tall
            cell = origin.atOffset(
                insideOffX * offX /* get sign back */,
                1,
                insideOffZ * offZ /* get sign back */);
            if (!blockProvider.getBlock(cell).isLaterallyPassable()) {
              reject(cell);
              continue outerZ;
            }
            cell = origin.atOffset(
                insideOffX * offX /* get sign back */,
                2,
                insideOffZ * offZ /* get sign back */);
            if (!blockProvider.getBlock(cell).isPassable()) {
              reject(cell);
              continue outerZ;
            }
          }
        }
        double jumpDistance = blockProvider.getBlock(origin.atOffset(offX, 1, offZ)).height()
            + 1.0
            - (blockProvider.getBlock(origin.atOffset(0, 0, 0)).isPassable()
            ? blockProvider.getBlock(origin.atOffset(0, -1, 0)).height() - 1
            : blockProvider.getBlock(origin.atOffset(0, 0, 0)).height());
        Cell other = origin.atOffset(offX, 1, offZ);
        if (!blockProvider.getBlock(origin.atOffset(offX, 0, offZ)).isVerticallyPassable()
            && jumpDistance <= 1.2) {
          // Can stand here
          accept(other, origin.distanceTo(other), options);
        } else {
          reject(other);
        }
      }
    }
    return options;
  }

  @Override
  public @NotNull ModeType type() {
    return ModeType.JUMP;
  }
}
