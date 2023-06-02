package net.whimxiqal.journey.search.function;

import net.whimxiqal.journey.Cell;

public class WeightedDistanceCostFunction extends CostFunction {

  private final DistanceFunction distanceFunction;
  private final Cell destination;
  private final double weight;

  public WeightedDistanceCostFunction(DistanceFunction distanceFunction, Cell destination, double weight) {
    this.distanceFunction = distanceFunction;
    this.destination = destination;
    this.weight = weight;
  }

  @Override
  public double apply(Cell cell, double existingCost) {
    return existingCost + weight * distanceFunction.distance(cell, destination);
  }

  @Override
  public Type type() {
    return Type.WEIGHTED_DISTANCE;
  }
}
