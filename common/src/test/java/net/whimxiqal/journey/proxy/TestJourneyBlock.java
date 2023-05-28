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

package net.whimxiqal.journey.proxy;

import java.util.Optional;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.platform.CellType;
import net.whimxiqal.journey.platform.TestPlatformProxy;
import net.whimxiqal.journey.platform.TestWorld;

public record TestJourneyBlock(Cell cell) implements JourneyBlock {

  @Override
  public boolean isNetherPortal() {
    return false;
  }

  @Override
  public boolean isWater() {
    return false;
  }

  @Override
  public boolean isPressurePlate() {
    return false;
  }

  @Override
  public boolean isClimbable() {
    return false;
  }

  @Override
  public boolean isPassable() {
    return !isBarrier();
  }

  @Override
  public boolean isLaterallyPassable() {
    return !isBarrier();
  }

  @Override
  public boolean isVerticallyPassable() {
    return !isBarrier();
  }

  private boolean isBarrier() {
    if (cell.blockY() < 0 || cell.blockY() > 1) {
      // We cannot go up and down, we are stuck in the horizontal plane
      return true;
    }
    TestWorld world = TestPlatformProxy.worlds.get(cell.domain());
    assert world != null;
    int flatWorldX = cell.blockX();
    int flatWorldY = cell.blockZ();
    if (flatWorldX < 0 || flatWorldX >= world.lengthX || flatWorldY < 0 || flatWorldY >= world.lengthY) {
      return true;  // this is past the border -- we can't go here
    }
    return world.cells[flatWorldY][flatWorldX] == CellType.BARRIER;  // this is a barrier cell -- we can't go here
  }

  @Override
  public boolean canStandOn() {
    return cell.blockY() == -1;
  }

  @Override
  public boolean canStandIn() {
    return !isBarrier();
  }

  @Override
  public float hardness() {
    return 1;
  }

  @Override
  public double height() {
    return 1;
  }

  @Override
  public Optional<JourneyDoor> asDoor() {
    return Optional.empty();
  }
}
