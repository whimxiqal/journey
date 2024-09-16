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

package net.whimxiqal.journey.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VectorTest {

  private static final Vector vec1 = new Vector(1, 0, 0);
  private static final Vector vec2 = new Vector(5, 7, 9);
  private static final Vector vec3 = new Vector(10, 3, -6);

  @Test
  void subtract() {
    Assertions.assertEquals(new Vector(-4, -7, -9), vec1.subtract(vec2));
  }

  @Test
  void times() {
    Assertions.assertEquals(new Vector(15, 21, 27), vec2.times(3));
  }

  @Test
  void projectionOnto() {
    Assertions.assertEquals(1.36547, vec3.projectionOnto(vec2), 0.0001);
  }

  @Test
  void dot() {
    Assertions.assertEquals(50 + 21 - 54, vec2.dot(vec3));
  }

  @Test
  void cross() {
    Assertions.assertEquals(new Vector(-69, 120, -55), vec2.cross(vec3));
  }

  @Test
  void magnitude() {
    Assertions.assertEquals(Math.sqrt(25 + 49 + 81), vec2.magnitude());
  }

  @Test
  void magnitudeSquared() {
    Assertions.assertEquals(25 + 49 + 81, vec2.magnitudeSquared());
  }

}