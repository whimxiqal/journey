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

/**
 * A generic location within 3-dimensional domain.
 * A domain is just a grid that is geometrically distinct from other domains.
 * A Minecraft world is considered a domain by Journey.
 */
public record Cell(int blockX, int blockY, int blockZ, int domain) {

  /**
   * Get the cartesian from one locatable to another, ignoring domain.
   * For comparisons between distances, use {@link #distanceToSquared(Cell)}
   * because it is easier to compute.
   *
   * @param other the other locatable
   * @return the cartesian distance
   */
  public double distanceTo(Cell other) {
    return Math.sqrt(distanceToSquared(other));
  }

  /**
   * Get the square of the Euclidean distance from one locatable to another.
   * This is much easier to compute than the actual distance because
   * you avoid a square root.
   *
   * @param other the other locatable
   * @return the square of the cartesian distance
   */
  public double distanceToSquared(Cell other) {
    return (blockX - other.blockX) * (blockX - other.blockX)
        + (blockY - other.blockY) * (blockY - other.blockY)
        + (blockZ - other.blockZ) * (blockZ - other.blockZ);
  }

  /**
   * Generate another {@link Cell} at a specific offset in the same domain.
   *
   * @param x the x coordinate
   * @param y the y coordinate
   * @param z the z coordinate
   * @return the new Cell
   */
  public Cell atOffset(int x, int y, int z) {
    return new Cell(blockX + x, blockY + y, blockZ + z, domain);
  }

  @Override
  public String toString() {
    return "{x: " + blockX + ", y: " + blockY + ", z: " + blockZ + ", domain: " + domain + "}";
  }

}
