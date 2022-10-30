package me.pietelite.journey.common.search.function;

import me.pietelite.journey.common.navigation.Cell;

public class HeightScoringFunction implements ScoringFunction {
  @Override
  public ScoringFunctionType getType() {
    return ScoringFunctionType.HEIGHT;
  }

  @Override
  public Double apply(Cell cell) {
    return (double) cell.getY(); // maximize height == maximize score
  }
}
