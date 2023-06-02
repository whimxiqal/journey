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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PlanarOrientedDistanceFunctionTest {

  private static final double DELTA = 0.001;
  private final DistanceFunction func = new PlanarOrientedDistanceFunction();
  private final static Cell ORIGIN = new Cell(0, 0, 0, 0);

  private void test(double expected, int x, int y, int z) {
    Assertions.assertEquals(expected, func.distance(ORIGIN, new Cell(x, y, z, 0)), DELTA);
  }

  @Test
  void apply() {
    test(0, 0, 0, 0);
    test(10, 10, 0, 0);
    test(10, 0, 0, 10);
    test(Math.sqrt(2) * 10, 0, 10, 0);  // must go diagonally up to go up (assuming we aren't using ladders or something like that)
    test(Math.sqrt(2) * 10, 10, 0, 10);
    test(Math.sqrt(3) * 10, 10, 10, 10);
    test(Math.sqrt(3) * 10 + Math.sqrt(2) * 20 + 30, 10, 30, 60);
    test(Math.sqrt(3) * 10 + Math.sqrt(2) * 20 + Math.sqrt(2) * 30, 30, 60, 10);
  }
}