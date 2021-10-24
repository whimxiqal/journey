/*
 * Copyright 2021 Pieter Svenson
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

package edu.whimc.journey.common.navigation;

import edu.whimc.journey.common.tools.AlternatingList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public final class Itinerary<T extends Cell<T, D>, D> {

  private final T origin;
  private final ArrayList<Step<T, D>> steps;
  private final AlternatingList<Leap<T, D>, Path<T, D>, Path<T, D>> stages;
  private final double length;

  public Itinerary(T origin, Collection<Step<T, D>> steps, AlternatingList<Leap<T, D>, Path<T, D>, Path<T, D>> stages, double length) {
    this.origin = origin;
    this.steps = new ArrayList<>(steps);
    this.stages = stages;
    this.length = length;
  }

  public T getOrigin() {
    return origin;
  }

  public ArrayList<Step<T, D>> getSteps() {
    return new ArrayList<>(steps);
  }

  public AlternatingList<Leap<T, D>, Path<T, D>, Path<T, D>> getStages() {
    return stages;
  }

  public double getLength() {
    return length;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Itinerary<?, ?> itinerary = (Itinerary<?, ?>) o;
    return Double.compare(itinerary.length, length) == 0
        && Objects.equals(origin, itinerary.origin)
        && Objects.equals(steps, itinerary.steps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(origin, steps, length);
  }
}
