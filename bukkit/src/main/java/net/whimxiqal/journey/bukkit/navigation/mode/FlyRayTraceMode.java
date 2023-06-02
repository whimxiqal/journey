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

import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.navigation.ModeType;
import org.bukkit.FluidCollisionMode;
import org.jetbrains.annotations.NotNull;

public class FlyRayTraceMode extends RayTraceMode {

  private static final int VOLUMETRIC_FREQUENCY = 16;

  public FlyRayTraceMode(Cell destination) {
    super(destination, 0.6, 1.8, 0.6, FluidCollisionMode.NEVER);
  }

  @Override
  public @NotNull ModeType type() {
    return ModeType.FLY;
  }

  @Override
  protected boolean shouldAttemptCalculation(Cell origin) {
    return Math.floorMod(origin.blockX(), VOLUMETRIC_FREQUENCY) == 0
        && Math.floorMod(origin.blockY(), VOLUMETRIC_FREQUENCY) == 0
        && Math.floorMod(origin.blockZ(), VOLUMETRIC_FREQUENCY) == 0;
  }

}
