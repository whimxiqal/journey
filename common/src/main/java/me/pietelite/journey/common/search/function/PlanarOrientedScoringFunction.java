package me.pietelite.journey.common.search.function;

import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.search.FlexiblePathTrial;

/**
 * This scoring function is similar to the Euclidean distance function but operates under the assumption that
 * a player should "walk" in the x-z dimensions and must move in the Y dimension using stairs or some path
 * 45 degrees offset from the x-z plane.
 */
public class PlanarOrientedScoringFunction implements ScoringFunction {

  private final static double SQRT_TWO = Math.sqrt(2);
  private final Cell destination;

  public PlanarOrientedScoringFunction(Cell destination) {
    this.destination = destination;
  }

  @Override
  public ScoringFunctionType getType() {
    return ScoringFunctionType.PLANAR_ORIENTED;
  }

  @Override
  public Double apply(Cell cell) {
    double diffX = cell.getX() - destination.getX();
    double diffYAbs = Math.abs(cell.getY() - destination.getY());
    double diffZ = cell.getZ() - destination.getZ();

    double toXZPlane = SQRT_TWO * diffYAbs;
    double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

    double alongXZ = Math.abs(diffXZ - diffYAbs);
    return -(alongXZ + toXZPlane);  // negative because we are trying to minimize distance / maximize score
  }
}
