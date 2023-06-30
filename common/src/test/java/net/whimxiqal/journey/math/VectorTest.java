package net.whimxiqal.journey.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VectorTest {

  private static final Vector vec1 = new Vector(1, 0, 0);
  private static final Vector vec2 = new Vector(5, 7, 9);
  private static final Vector vec3 = new Vector(10, 3, -6);

  @Test
  void subtract() {
    Assertions.assertEquals(new Vector(-4, -7, -9), vec1.subtract(vec2));
  }

  @Test
  void times() {
    Assertions.assertEquals(new Vector(15, 21, 27), vec2.times(3));
  }

  @Test
  void projectionOnto() {
    Assertions.assertEquals(1.36547, vec3.projectionOnto(vec2), 0.0001);
  }

  @Test
  void dot() {
    Assertions.assertEquals(50 + 21 - 54, vec2.dot(vec3));
  }

  @Test
  void cross() {
    Assertions.assertEquals(new Vector(-69, 120, -55), vec2.cross(vec3));
  }

  @Test
  void magnitude() {
    Assertions.assertEquals(Math.sqrt(25 + 49 + 81), vec2.magnitude());
  }

  @Test
  void magnitudeSquared() {
    Assertions.assertEquals(25 + 49 + 81, vec2.magnitudeSquared());
  }

}