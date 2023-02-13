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

package net.whimxiqal.journey.search.function;

import net.whimxiqal.journey.Cell;

/**
 * This scoring function is similar to the Euclidean distance function but operates under the assumption that
 * a player should "walk" in the x-z dimensions and must move in the Y dimension using stairs or some path
 * 45 degrees offset from the x-z plane.
 */
public class PlanarOrientedCostFunction implements CostFunction {

  private final static double SQRT_TWO = Math.sqrt(2);
  private final Cell destination;

  public PlanarOrientedCostFunction(Cell destination) {
    this.destination = destination;
  }

  @Override
  public CostFunctionType getType() {
    return CostFunctionType.PLANAR_ORIENTED;
  }

  @Override
  public Double apply(Cell cell) {
    double diffX = cell.blockX() - destination.blockX();
    double diffYAbs = Math.abs(cell.blockY() - destination.blockY());
    double diffZ = cell.blockZ() - destination.blockZ();

    double toXZPlane = SQRT_TWO * diffYAbs;
    double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

    double alongXZ = Math.abs(diffXZ - diffYAbs);
    return alongXZ + toXZPlane;
  }
}
