package me.pietelite.journey.common.search.function;

import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.search.FlexiblePathTrial;

public class EuclideanDistanceScoringFunction implements ScoringFunction {

  private final Cell destination;

  public EuclideanDistanceScoringFunction(Cell destination) {
    this.destination = destination;
  }

  @Override
  public ScoringFunctionType getType() {
    return ScoringFunctionType.EUCLIDEAN_DISTANCE;
  }

  @Override
  public Double apply(Cell cell) {
    return cell.distanceToSquared(destination);
  }
}
