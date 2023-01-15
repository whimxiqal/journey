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
import net.whimxiqal.journey.common.navigation.ModeType;
import net.whimxiqal.journey.common.search.SearchSession;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * A mode to provide the results to whether a player can climb blocks around them,
 * like ladders or vines.
 */
public final class ClimbMode extends BukkitMode {

  private static final Set<Material> climbable = new HashSet<>(Arrays.asList(Material.LADDER, Material.VINE));

  /**
   * General constructor.
   *
   * @param forcePassable a set of materials deemed passable
   */
  public ClimbMode(SearchSession session, Set<Material> forcePassable) {
    super(session, forcePassable);
  }

  @Override
  protected void collectDestinations(@NotNull Cell origin, @NotNull List<Option> options) {

    // TODO we have to make sure that the ladders and vines are oriented correctly
    //  and that the vines have a solid block behind it
    tryToClimbAdjacent(origin.atOffset(1, 0, 0), options);
    tryToClimbAdjacent(origin.atOffset(-1, 0, 0), options);
    tryToClimbAdjacent(origin.atOffset(0, 0, 1), options);
    tryToClimbAdjacent(origin.atOffset(0, 0, -1), options);
    tryToClimbAdjacent(origin.atOffset(0, -1, 0), options);

    // Going up is a different story
    if (climbable.contains(BukkitUtil.getBlock(origin).getMaterial())) {
      if (isVerticallyPassable(BukkitUtil.getBlock(origin.atOffset(0, 1, 0)))
          && isVerticallyPassable(BukkitUtil.getBlock(origin.atOffset(0, 2, 0)))) {
        accept(origin.atOffset(0, 1, 0), 1.0d, options);
      } else {
        reject(origin.atOffset(0, 1, 0));
      }
    }

  }

  private void tryToClimbAdjacent(Cell cell, List<Option> options) {
    if (climbable.contains(BukkitUtil.getBlock(cell).getMaterial())) {
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
