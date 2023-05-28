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

package net.whimxiqal.journey.search.function;

import net.whimxiqal.journey.Cell;

/**
 * This scoring function is similar to the Euclidean distance function but operates under the assumption that
 * a player should "walk" in the x-z dimensions and must move in the Y dimension using stairs or some path
 * 45 degrees offset from the x-z plane.
 */
public record PlanarOrientedCostFunction(Cell destination) implements CostFunction {

  private final static double SQRT_TWO = Math.sqrt(2);
  private final static double SQRT_THREE = Math.sqrt(3);

  @Override
  public CostFunctionType getType() {
    return CostFunctionType.PLANAR_ORIENTED;
  }

  @Override
  public Double apply(Cell cell) {
    // Sort coordinates in order of size
    int xDiff = Math.abs(destination.blockX() - cell.blockX());
    int yDiff = Math.abs(destination.blockY() - cell.blockY());
    int zDiff = Math.abs(destination.blockZ() - cell.blockZ());
    int first, second, third;
    boolean isYFirst = false;
    if (yDiff > xDiff && yDiff > zDiff) {
      first = yDiff;
      isYFirst = true;
      if (xDiff > zDiff) {
        second = xDiff;
        third = zDiff;
      } else {
        second = zDiff;
        third = xDiff;
      }
    } else if (xDiff > zDiff) {
      first = xDiff;
      if (yDiff > zDiff) {
        second = yDiff;
        third = zDiff;
      } else {
        second = zDiff;
        third = yDiff;
      }
    } else {
      first = zDiff;
      if (xDiff > yDiff) {
        second = xDiff;
        third = yDiff;
      } else {
        second = yDiff;
        third = xDiff;
      }
    }

    // Calculate distance to travel
    double total = SQRT_THREE * third;
    second -= third;
    first -= third;

    total += SQRT_TWO * second;
    first -= second;

    if (isYFirst) {
      total += first * SQRT_TWO;
    } else {
      total += first;
    }
    return total;
  }

  @Override
  public String toString() {
    return "PlanarOrientedCostFunction{destination:" + destination + "}";
  }
}
