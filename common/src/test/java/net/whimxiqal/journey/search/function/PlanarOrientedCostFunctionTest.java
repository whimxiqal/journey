package net.whimxiqal.journey.search.function;

import net.whimxiqal.journey.Cell;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlanarOrientedCostFunctionTest {

  private static final double DELTA = 0.001;
  private final CostFunction func = new PlanarOrientedCostFunction(new Cell(0, 0, 0, 0));

  private void test(double expected, int x, int y, int z) {
    Assertions.assertEquals(expected, func.apply(new Cell(x, y, z, 0)), DELTA);
  }

  @Test
  void apply() {
    test(0, 0, 0, 0);
    test(10, 10, 0, 0);
    test(10, 0, 0, 10);
    test(Math.sqrt(2) * 10, 0, 10, 0);  // must go diagonally up to go up (assuming we aren't using ladders or something like that)
    test(Math.sqrt(2) * 10, 10, 0, 10);
    test(Math.sqrt(3) * 10, 10, 10, 10);
    test(Math.sqrt(3) * 10 + Math.sqrt(2) * 20 + 30, 10, 30, 60);
    test(Math.sqrt(3) * 10 + Math.sqrt(2) * 20 + Math.sqrt(2) * 30, 30, 60, 10);
  }
}