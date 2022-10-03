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

package me.pietelite.journey.common.navigation;

import java.io.Serializable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * A generic unit of a 3-dimensional grid (domain).
 */
public class Cell implements Locatable<Cell>, Serializable {

  private final int coordinateX;
  private final int coordinateY;
  private final int coordinateZ;
  private final String domainId;

  /**
   * General constructor.
   *
   * @param x              the X coordinate
   * @param y              the Y coordinate
   * @param z              the Z coordinate
   * @param domainId       the identifier for the domain
   */
  public Cell(int x, int y, int z, @NotNull String domainId) {
    this.coordinateX = x;
    this.coordinateY = y;
    this.coordinateZ = z;
    this.domainId = Objects.requireNonNull(domainId);
  }

  public double distanceToSquared(Cell other) {
    return vectorSizeSquared(this.coordinateX - other.coordinateX,
        this.coordinateY - other.coordinateY,
        this.coordinateZ - other.coordinateZ);
  }

  private double vectorSizeSquared(int distX, int distY, int distZ) {
    return distX * distX + distY * distY + distZ * distZ;
  }

  /**
   * Get X coordinate.
   *
   * @return x coordinate
   */
  public final int getX() {
    return coordinateX;
  }

  /**
   * Get Y coordinate.
   *
   * @return y coordinate
   */
  public final int getY() {
    return coordinateY;
  }

  /**
   * Get Z coordinate.
   *
   * @return z coordinate
   */
  public final int getZ() {
    return coordinateZ;
  }

  /**
   * Get the id of the domain.
   *
   * @return the domain id
   */
  public final String domainId() {
    return domainId;
  }

  public Cell atOffset(int x, int y, int z) {
    return new Cell(this.coordinateX + x,
        this.coordinateY + y,
        this.coordinateZ + z,
        this.domainId);
  }

}
