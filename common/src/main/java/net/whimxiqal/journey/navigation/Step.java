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

package net.whimxiqal.journey.navigation;

import java.io.Serializable;
import java.util.Objects;
import net.whimxiqal.journey.Cell;
import org.jetbrains.annotations.NotNull;

/**
 * A representation of a movement step between {@link Cell}s on a {@link Path}.
 */
public class Step implements Serializable, Moded {
  private final Cell location;
  private final double length;
  private final ModeType modeType;

  public Step(@NotNull Cell location, double length, @NotNull ModeType modeType) {
    this.location = location;
    this.length = length;
    this.modeType = modeType;
  }

  public Cell location() {
    return location;
  }

  public double length() {
    return length;
  }

  @Override
  public @NotNull ModeType modeType() {
    return modeType;
  }

  @Override
  public String toString() {
    return "Step{" +
        "location=" + location +
        ", length=" + length +
        ", modeType=" + modeType +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Step step = (Step) o;
    return Double.compare(step.length, length) == 0 && location.equals(step.location) && modeType == step.modeType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(location, length, modeType);
  }
}
