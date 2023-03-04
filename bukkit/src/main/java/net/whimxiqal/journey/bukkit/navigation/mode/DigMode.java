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

package net.whimxiqal.journey.bukkit.navigation.mode;

import java.util.List;
import java.util.Set;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.navigation.Mode;
import net.whimxiqal.journey.navigation.ModeType;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class DigMode extends BukkitMode {

  public final static double DIG_COST_MULTIPLIER = 16;

  /**
   * General constructor.
   *
   * @param session       the session
   * @param forcePassable the list of passable materials
   */
  public DigMode(SearchSession session, Set<Material> forcePassable) {
    super(session, forcePassable);
  }

  @Override
  protected void collectDestinations(@NotNull Cell origin, @NotNull List<Mode.Option> options) {
    // Can we even stand here?
    if (!canStandOn(BukkitUtil.getBlock(origin.atOffset(0, -1, 0)))
        && !canStandIn(BukkitUtil.getBlock(origin.atOffset(0, 0, 0)))) {
      return;
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
          float digTime = 0;
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
                if (!isLaterallyPassable(BukkitUtil.getBlock(cell))) {
                  // we must break it
                  if (BukkitUtil.getBlock(cell).getMaterial().getHardness() < 0) {
                    reject(cell);
                    continue outerZ;
                  } else {
                    digTime += BukkitUtil.getBlock(cell).getMaterial().getHardness();
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
                  if (!isPassable(BukkitUtil.getBlock(cell))) {
                    if (BukkitUtil.getBlock(cell).getMaterial().getHardness() < 0) {
                      reject(cell);
                      continue outerZ;
                    } else {
                      digTime += BukkitUtil.getBlock(cell).getMaterial().getHardness();
                    }
                  }
                }
              }
            }
          }
          Cell other = origin.atOffset(offX, offY, offZ);
          accept(other, origin.distanceTo(other) + digTime * DIG_COST_MULTIPLIER, options);
        }
      }
    }
  }

  @Override
  public @NotNull ModeType type() {
    return ModeType.DIG;
  }
}
