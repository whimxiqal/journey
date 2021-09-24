/*
 * Copyright 2021 Pieter Svenson
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

package edu.whimc.indicator.spigot.navigation;

import edu.whimc.indicator.common.navigation.Cell;
import edu.whimc.indicator.spigot.util.UuidToWorld;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class LocationCell extends Cell<LocationCell, World> {

  /**
   * The offset above the block location (for solid inhabitable blocks)
   */
  @Getter
  private final double heightOffset;

  public LocationCell(int x, double y, int z, @NotNull World world) {
    this(x, y, z, world.getUID());
  }

  public LocationCell(Location location) {
    this(location.getBlockX(), location.getBlockY(), location.getBlockZ(), Objects.requireNonNull(location.getWorld()));
  }

  public LocationCell(int x, double y, int z, @NotNull UUID worldUuid) {
    super(x, (int) y, z, worldUuid.toString(), new UuidToWorld());
    this.heightOffset = y - this.getY();
  }

  @Override
  public double distanceToSquared(LocationCell other) {
    return vectorSizeSquared(this.x - other.x,
        this.y - other.y,
        this.z - other.z);
  }

  public Block getBlock() {
    return this.getDomain().getBlockAt(this.x, this.y, this.z);
  }

  public Block getBlockAtOffset(int x, int y, int z) {
    return this.getDomain().getBlockAt(this.x + x, this.y + y, this.z + z);
  }

  public LocationCell createLocatableAtOffset(int x, double y, int z) {
    return new LocationCell(this.x + x, this.y + heightOffset + y, this.z + z, this.getDomain());
  }

  public boolean hasHeightOffset() {
    return heightOffset != 0;
  }

  private double vectorSizeSquared(int distX, int distY, int distZ) {
    return distX * distX + distY * distY + distZ * distZ;
  }

  @Override
  public String toString() {
    return String.format("(%d, %d, %d) in %s", this.x, this.y, this.z, this.getDomain().getName());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LocationCell that = (LocationCell) o;
    return this.x == that.x && this.y == that.y && this.z == that.z && this.getDomain().equals(that.getDomain());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.x, this.y, this.z, this.getDomain());
  }
}
