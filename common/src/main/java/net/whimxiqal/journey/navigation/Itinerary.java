/*
 * MIT License
 *
 * Copyright (c) whimxiqal
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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.search.ItineraryTrial;
import net.whimxiqal.journey.search.SearchSession;

/**
 * A description of all step required to move from some arbitrary origin
 * to some arbitrary destination.
 * It is thread-safe because everything is read-only.
 *
 * <p>The result of a {@link SearchSession} calculation.
 *
 * @see SearchSession
 * @see ItineraryTrial
 * @see Navigator
 */
public record Itinerary(Cell origin, List<Step> steps, double cost) {

  /**
   * General constructor.
   *
   * @param origin the origin of the itinerary
   * @param steps  the steps to get to the destination
   * @param stages the list of stages to complete to traverse this itinerary
   * @param cost   the length of the entire thing
   */
  public Itinerary(Cell origin,
                   List<Step> steps,
                   double cost) {
    this.origin = origin;
    this.steps = Collections.unmodifiableList(steps);
    this.cost = cost;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Itinerary itinerary = (Itinerary) o;
    return Double.compare(itinerary.cost, cost) == 0
        && Objects.equals(origin, itinerary.origin)
        && Objects.equals(steps, itinerary.steps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(origin, steps, cost);
  }
}
