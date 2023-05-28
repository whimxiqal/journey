package net.whimxiqal.journey.chunk;

public enum Direction {
  POSITIVE_X,
  NEGATIVE_X,
  POSITIVE_Y,
  NEGATIVE_Y,
  POSITIVE_Z,
  NEGATIVE_Z;

  public static Direction from(int x, int y, int z) {
    if (Math.abs(x) + Math.abs(y) + Math.abs(z) != 1) {
      throw new IllegalArgumentException(String.format("Only one of the coordinates may be 1, all others must be 0. Found: x=%d, y=%d, z=%d", x, y, z));
    }
    if (x == 1) {
      return POSITIVE_X;
    } else if (x == -1) {
      return NEGATIVE_X;
    } else if (y == 1) {
      return POSITIVE_Y;
    } else if (y == -1) {
      return NEGATIVE_Y;
    } else if (z == 1) {
      return POSITIVE_Z;
    }
    return NEGATIVE_Z;
  }

}