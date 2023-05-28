package net.whimxiqal.journey.search.function;

import net.whimxiqal.journey.Cell;

/**
 * A simple cost function that just returns 0 as the cost of every cell.
 * Should be used a placeholder for an actual cost function.
 */
public class ZeroCostFunction implements CostFunction {
  @Override
  public CostFunctionType getType() {
    return CostFunctionType.OTHER;
  }

  @Override
  public Double apply(Cell cell) {
    return 0.0;
  }
}
