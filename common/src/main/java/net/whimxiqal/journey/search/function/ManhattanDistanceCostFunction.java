package net.whimxiqal.journey.search.function;

import net.whimxiqal.journey.Cell;

public class ManhattanDistanceCostFunction implements CostFunction {
  @Override
  public CostFunctionType getType() {
    return CostFunctionType.MANHATTAN_DISTANCE;
  }

  private final Cell destination;

  public ManhattanDistanceCostFunction(Cell destination) {
    this.destination = destination;
  }

  @Override
  public Double apply(Cell cell) {
    return (double) Math.abs(cell.blockX() - destination.blockX())
        + Math.abs(cell.blockY() - destination.blockY())
        + Math.abs(cell.blockZ() - destination.blockZ());
  }
}
