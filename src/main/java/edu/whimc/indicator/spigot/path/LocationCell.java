package edu.whimc.indicator.spigot.path;

import edu.whimc.indicator.api.path.Cell;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;

public class LocationCell extends Cell<LocationCell, World> {

  public LocationCell(int x, int y, int z, World world) {
    super(x, y, z, world);
  }

  public LocationCell(Location location) {
    super(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld());
  }

  @Override
  public double distanceToSquared(LocationCell other) {
    return vectorSizeSquared(this.x - other.x,
        this.y - other.y,
        this.z - other.z);
  }

  public Block getBlockAtOffset(int x, int y, int z) {
    return this.domain.getBlockAt(this.x + x, this.y + y, this.z + z);
  }

  public LocationCell createLocatableAtOffset(int x, int y, int z) {
    return new LocationCell(this.x + x, this.y + y, this.z + z, this.domain);
  }

  private double vectorSizeSquared(int distX, int distY, int distZ) {
    return distX * distX + distY * distY + distZ * distZ;
  }

  @Override
  public String print() {
    return String.format("(%d, %d, %d) in %s", this.x, this.y, this.z, this.domain.getName());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LocationCell that = (LocationCell) o;
    return this.x == that.x && this.y == that.y && this.z == that.z && this.domain.equals(that.domain);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.x, this.y, this.z, this.domain);
  }
}
