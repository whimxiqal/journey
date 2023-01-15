/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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

import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.journey.common.navigation.Mode;
import net.whimxiqal.journey.common.navigation.ModeType;
import net.whimxiqal.journey.common.search.SearchSession;
import java.util.List;
import java.util.Set;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import net.whimxiqal.journey.bukkit.util.MaterialGroups;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link Mode}
 * to check the places a player may jump to in Minecraft.
 */
public class JumpMode extends BukkitMode {

  /**
   * General constructor.
   *
   * @param session       the search session
   * @param forcePassable the set of passable materials
   */
  public JumpMode(SearchSession session, Set<Material> forcePassable) {
    super(session, forcePassable);
  }

  @Override
  public void collectDestinations(@NotNull Cell origin, @NotNull List<Option> options) {
    Cell cell;

    cell = origin.atOffset(0, -1, 0);
    if (isVerticallyPassable(BukkitUtil.getBlock(cell))) {
      // Nothing to jump off of
      reject(cell);
      return;
    }

    cell = origin.atOffset(0, 2, 0);
    if (!isVerticallyPassable(BukkitUtil.getBlock(cell))) {
      // No room to jump
      reject(cell);
      return;
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
            if (!isLaterallyPassable(BukkitUtil.getBlock(cell))) {
              reject(cell);
              continue outerZ;
            }
            cell = origin.atOffset(
                insideOffX * offX /* get sign back */,
                2,
                insideOffZ * offZ /* get sign back */);
            if (!isPassable(BukkitUtil.getBlock(cell))) {
              reject(cell);
              continue outerZ;
            }
          }
        }
        double jumpDistance = MaterialGroups.height(BukkitUtil.getBlock(origin.atOffset(offX, 1, offZ)).getMaterial())
            + 1.0
            - (MaterialGroups.isPassable(BukkitUtil.getBlock(origin.atOffset(0, 0, 0)).getMaterial())
            ? MaterialGroups.height(BukkitUtil.getBlock(origin.atOffset(0, -1, 0)).getMaterial()) - 1
            : MaterialGroups.height(BukkitUtil.getBlock(origin.atOffset(0, 0, 0)).getMaterial()));
        Cell other = origin.atOffset(offX, 1, offZ);
        if (!isVerticallyPassable(BukkitUtil.getBlock(origin.atOffset(offX, 0, offZ)))
            && jumpDistance <= 1.2) {
          // Can stand here
          accept(other, origin.distanceTo(other), options);
        } else {
          reject(other);
        }
      }
    }
  }

  @Override
  public @NotNull ModeType type() {
    return ModeType.JUMP;
  }
}
