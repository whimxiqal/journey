package net.whimxiqal.journey.navigation;

import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.math.Vector;

public class NavigationStep {

  private final int domain;
  private final Vector startVector;
  private final Vector path;

  private final double totalLength;
  private final Cell destination;

  public NavigationStep(Cell origin, Cell destination) {
    this.domain = origin.domain();
    this.startVector = new Vector(origin.blockX(), origin.blockY(), origin.blockZ());
    this.path = new Vector(destination.blockX() - origin.blockX(),
        destination.blockY() - origin.blockY(),
        destination.blockZ() - origin.blockZ());
    this.totalLength = path.magnitude();
    this.destination = destination;
  }

  double length() {
    return totalLength;
  }

  public int domain() {
    return domain;
  }

  public Vector startVector() {
    return startVector;
  }

  public Vector path() {
    return path;
  }

  public Cell destination() {
    return destination;
  }

  @Override
  public String toString() {
    return "NavigationStep{" +
        "startVector=" + startVector +
        ", path=" + path +
        ", destination=" + destination +
        '}';
  }
}
