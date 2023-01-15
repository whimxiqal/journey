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

package net.whimxiqal.journey.platform;

import java.util.List;
import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.journey.common.navigation.Mode;
import net.whimxiqal.journey.common.navigation.ModeType;
import net.whimxiqal.journey.common.search.SearchSession;
import org.jetbrains.annotations.NotNull;

public class WalkMode extends Mode {
  public WalkMode(@NotNull SearchSession session) {
    super(session);
  }

  @Override
  protected void collectDestinations(@NotNull Cell origin, @NotNull List<Option> options) {
    TestWorld world = TestPlatformProxy.worlds.get(origin.domainId());
    assert world != null;
    for (int[] pair : new int[][]{{origin.getX() - 1, origin.getY()},
        {origin.getX() + 1, origin.getY()},
        {origin.getX(), origin.getY() - 1},
        {origin.getX(), origin.getY() + 1}}) {
      int x = pair[0];
      int y = pair[1];
      if (x < 0 || x >= world.lengthX || y < 0 || y >= world.lengthY) {
        continue;  // this is past the border -- we can't go here
      }
      if (world.cells[y][x] == CellType.BARRIER) {
        continue;  // this is a barrier cell -- we can't go here
      }
      options.add(Option.between(origin, x, y, 0));
    }
  }

  @Override
  public @NotNull ModeType type() {
    return ModeType.WALK;
  }
}
