package edu.whimc.indicator.spigot.path;

import edu.whimc.indicator.api.path.Completion;
import edu.whimc.indicator.api.path.Link;
import edu.whimc.indicator.spigot.util.NetherUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class NetherLink implements Link<LocationCell, World> {

  private final LocationCell origin;
  private final LocationCell destination;

  public NetherLink(@NotNull final LocationCell origin, @NotNull final LocationCell destination) {
    // TODO verify input
    this.origin = origin;
    this.destination = destination;
  }

  @Override
  public LocationCell getOrigin() {
    return origin;
  }

  @Override
  public LocationCell getDestination() {
    return destination;
  }

  @Override
  public Completion<LocationCell, World> getCompletion() {
    return loc -> loc.distanceToSquared(origin) < 9;
  }

  @Override
  public double weight() {
    return 16;
  }

  @Override
  public boolean verify() {
    return NetherUtil.locateAll(origin, 1).size() > 0
        && NetherUtil.locateAll(destination, 1).size() > 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NetherLink that = (NetherLink) o;
    return getOrigin().equals(that.getOrigin()) && getDestination().equals(that.getDestination());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getOrigin(), getDestination());
  }

  @Override
  public String toString() {
    return "NetherLink: "
        + origin + " -> "
        + destination;
  }
}
