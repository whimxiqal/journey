package edu.whimc.indicator.util;

import edu.whimc.indicator.api.path.Cell;
import org.bukkit.World;

public final class LocationUtil {

  private LocationUtil() {
  }

  /**
   * Determine if the user can step from the given block to a
   * block one block away in the X or Z direction and some
   * amount of blocks up or down.
   *
   * Does no checks for whether a player can stand at the given
   * origin points.
   * @param cell the cell to check against
   * @param offY the offset of the step in y
   * @param xStep which a axis a step is being taken: x if true, z if false
   * @param isPositive which direction a step is being taken:
   *                   positive if true, negative if false
   * @param <C> the type of locatable
   * @return true if a player can step there
   */
  private static <C extends Cell<C, World>> boolean canStep(C cell, int offY,
                                                            boolean xStep, boolean isPositive) {
    if (offY > 1 || offY < -3) return false;  // Too high to jump, too low to fall

    int floorX = cell.getX() + (xStep ? 1 : 0)*(isPositive ? 1 : -1);
    int floorY = cell.getY() - 1 + offY;
    int floorZ = cell.getZ() + (xStep ? 0 : 1)*(isPositive ? 1 : -1);

    if (cell.getDomain().getBlockAt(floorX, floorY, floorZ).isPassable()) {
      return false;  // There is no floor
    }

    for (int columnY = Math.min(floorY, cell.getY()) + 1; columnY <= Math.max(floorY, cell.getY()) + 1; columnY++) {
      if (!cell.getDomain().getBlockAt(floorX, columnY, floorY).isPassable()) {
        return false;
      }
    }

    return true;
  }

  public static <C extends Cell<C, World>> boolean canStepXPos(C cell, int offY) {
    return canStep(cell, offY, true, true);
  }

  public static <C extends Cell<C, World>> boolean canStepXNeg(C cell, int offY) {
    return canStep(cell, offY, true, false);
  }

  public static <C extends Cell<C, World>> boolean canStepZPos(C cell, int offY) {
    return canStep(cell, offY, false, true);
  }

  public static <C extends Cell<C, World>> boolean canStepZNeg(C cell, int offY) {
    return canStep(cell, offY, false, false);
  }

}
