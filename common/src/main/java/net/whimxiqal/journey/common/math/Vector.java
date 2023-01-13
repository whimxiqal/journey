/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.whimxiqal.journey.common.math;

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
