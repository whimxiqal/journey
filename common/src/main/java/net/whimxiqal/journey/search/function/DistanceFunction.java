package net.whimxiqal.journey.search.function;

import net.whimxiqal.journey.Cell;

public abstract class DistanceFunction {

  abstract public double distance(Cell origin, Cell destination);

  abstract public Type type();

  enum Type {
    EUCLIDEAN,
    PLANAR,
    MANHATTAN,
  }

  @Override
  public String toString() {
    return type().name();
  }
}
