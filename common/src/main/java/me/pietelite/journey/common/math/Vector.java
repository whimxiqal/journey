package me.pietelite.journey.common.math;

public class Vector {
  private final double x;
  private final double y;
  private final double z;
  
  public Vector(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public double x() {
    return x;
  }

  public double y() {
    return y;
  }

  public double z() {
    return z;
  }
  
  public Vector subtract(Vector other) {
    return new Vector(x - other.x, y - other.y, z - other.z);
  }

  public Vector times(double factor) {
    return new Vector(x * factor, y * factor, z * factor);
  }

  public double projectionOnto(Vector other) {
    return dot(other) / other.magnitude();
  }

  public double dot(Vector other) {
    return x * other.x + y * other.y + z * other.z;
  }

  public double magnitude() {
    return Math.sqrt(magnitudeSquared());
  }

  public double magnitudeSquared() {
    return (x * x) + (y * y) + (z * z);
  }

  public Vector unit() {
    return times(1d / magnitude());
  }

  @Override
  public String toString() {
    return "Vector{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
  }
}
