package net.whimxiqal.journey.search.function;

import net.whimxiqal.journey.Cell;

public class DistanceCostFunction extends CostFunction {
  private final DistanceFunction distanceFunction;
  private final Cell destination;

  public DistanceCostFunction(DistanceFunction distanceFunction, Cell destination) {
    this.distanceFunction = distanceFunction;
    this.destination = destination;
  }

  @Override
  public double apply(Cell cell, double existingCost) {
    return existingCost + distanceFunction.distance(cell, destination);
  }

  @Override
  public Type type() {
    return Type.DISTANCE;
  }
}
