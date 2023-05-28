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
 * for normal walking in Minecraft.
 */
public class WalkMode extends Mode {

  public WalkMode(@NotNull SearchSession session) {
    super(session);
  }

  @Override
  public Collection<Option> getDestinations(Cell origin, BlockProvider blockProvider) throws ExecutionException, InterruptedException {
    List<Option> options = new LinkedList<>();
    Cell cell;
    Cell cell1;
    Cell cell2;
//    // Can you drop into an inhabitable block?
//    cell = origin.atOffset(0, -1, 0);
//    if (canStandOn(BukkitUtil.getBlock(origin.atOffset(0, -2, 0))) && isVerticallyPassable(BukkitUtil.getBlock(cell))) {
//      accept(cell, 1.0d, options);
//    } else {
//      reject(cell);
//    }

    // Can we even stand here?
    if (!blockProvider.getBlock(origin.atOffset(0, -1, 0)).canStandOn()
        && !blockProvider.getBlock(origin).canStandIn()) {
      return options;
    }

    // 1 block away
    for (int offX = -1; offX <= 1; offX++) {
      outerZ:
      for (int offZ = -1; offZ <= 1; offZ++) {
        // For the diagonal points, check that the path is clear in both
        //  lateral directions and diagonally
        for (int insideOffX = offX * offX /* normalize sign */; insideOffX >= 0; insideOffX--) {
          for (int insideOffZ = offZ * offZ /* normalize sign */; insideOffZ >= 0; insideOffZ--) {
            if (insideOffX == 0 && insideOffZ == 0) {
              continue;
            }
            for (int offY = 0; offY <= 1; offY++) { // Check two blocks tall
              cell = origin.atOffset(insideOffX * offX /* get sign back */,
                  offY,
                  insideOffZ * offZ /*get sign back */);
              if (!blockProvider.getBlock(cell).isLaterallyPassable()) {
                reject(cell);
                continue outerZ;  // Barrier - invalid move
              }
            }
          }
        }
        // We can move to offX and offZ laterally

        // check at our y coordinate (0 offset)
        cell = origin.atOffset(offX, 0, offZ);
        if (offX != 0 || offZ != 0) {
          // we are inquiring about other than origin
          if (blockProvider.getBlock(cell).canStandIn()) {
            // We can just stand right here (carpets, etc.)
            accept(cell, origin.distanceTo(cell), options);
            continue;
          } else {
            reject(cell);
          }
        }
        for (int offY = -1; offY >= -4; offY--) {  // Check for floor anywhere up to a 3 block fall
          cell = origin.atOffset(offX, offY, offZ);  // floor
          cell1 = cell.atOffset(0, 1, 0);  // feet
          cell2 = cell.atOffset(0, 2, 0);  // head
          if (!blockProvider.getBlock(cell2).isVerticallyPassable() || blockProvider.getBlock(cell2).isWater()) {
            // we cannot "fall through" this cell, which means we can't land on the block below
            // or, we are drowning
            reject(cell1);
            break;
          }
          if (!blockProvider.getBlock(cell1).isVerticallyPassable()) {
            // we cannot put our feet in here
            reject(cell1);
            continue;
          }
          if (!blockProvider.getBlock(cell).canStandOn()) {
            // we cannot stand on here
            reject(cell1);
            continue;
          }
          // good
          accept(cell1, origin.distanceTo(cell1), options);
          break;
        }
      }
    }
    return options;
  }

  @Override
  public @NotNull ModeType type() {
    return ModeType.WALK;
  }
}
