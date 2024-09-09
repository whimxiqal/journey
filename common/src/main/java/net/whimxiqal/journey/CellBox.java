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

package net.whimxiqal.journey;

public class CellBox {

  private final Cell min;
  private final Cell max;
  private final Cell center;

  CellBox(Cell point1, Cell point2) {
    if (point1.domain() != point2.domain()) {
      throw new IllegalArgumentException("Min domain " + point1.domain() + " does not equal max domain " + point1.domain());
    }
    this.min = new Cell(Math.min(point1.blockX(), point2.blockX()),
        Math.min(point1.blockY(), point2.blockY()),
        Math.min(point1.blockZ(), point2.blockZ()),
        point1.domain());
    this.max = new Cell(Math.max(point1.blockX(), point2.blockX()),
        Math.max(point1.blockY(), point2.blockY()),
        Math.max(point1.blockZ(), point2.blockZ()),
        point1.domain());
    this.center = new Cell((this.min.blockX() + this.max.blockX()) / 2,
        (this.min.blockY() + this.max.blockY()) / 2,
        (this.min.blockZ() + this.max.blockZ()) / 2,
        point1.domain());
  }

  public Cell center() {
    return this.center;
  }

  public boolean contains(Cell location) {
    return this.min.blockX() <= location.blockX() && location.blockX() <= this.max.blockX()
        && this.min.blockY() <= location.blockY() && location.blockY() <= this.max.blockY()
        && this.min.blockZ() <= location.blockZ() && location.blockZ() <= this.max.blockZ();
  }

}
