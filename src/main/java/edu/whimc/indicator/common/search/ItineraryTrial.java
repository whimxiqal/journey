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

package edu.whimc.indicator.common.search;

import edu.whimc.indicator.common.navigation.Cell;
import edu.whimc.indicator.common.navigation.Itinerary;
import edu.whimc.indicator.common.navigation.Leap;
import edu.whimc.indicator.common.navigation.Mode;
import edu.whimc.indicator.common.navigation.Path;
import edu.whimc.indicator.common.navigation.Step;
import edu.whimc.indicator.common.tools.AlternatingList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class ItineraryTrial<T extends Cell<T, D>, D> {

  private final T origin;
  private final AlternatingList<Leap<T, D>, PathTrial<T, D>, Object> alternatingList;

  public ItineraryTrial(T origin, AlternatingList<Leap<T, D>, PathTrial<T, D>, Object> alternatingList) {
    this.origin = origin;
    this.alternatingList = alternatingList;
  }

  @NotNull
  public Optional<Itinerary<T, D>> attempt(SearchSession<T, D> session, Collection<Mode<T, D>> modes, boolean useCache) {
    boolean failed = false;
    for (PathTrial<T, D> pathTrial : alternatingList.getMinors()) {
      Optional<Path<T, D>> optionalPath = pathTrial.attempt(session, modes, useCache);
      if (!optionalPath.isPresent()) {
        failed = true;
      }
    }
    if (failed) {
      return Optional.empty();
    }

    // accumulate length
    double length = 0;
    for (Leap<T, D> leap : alternatingList.getMajors()) {
      if (leap != null) {
        length += leap.getLength();
      }
    }
    for (PathTrial<T, D> pathTrial : alternatingList.getMinors()) {
      length += pathTrial.getLength();
    }

    List<List<Step<T, D>>> flattenedList = alternatingList.flatten(leap -> {
      if (leap == null) {
        return null;
      } else {
        return leap.getSteps();
      }
    }, trial -> trial.getPath().getSteps() /* Path must exist because we didn't fail */);
    List<Step<T, D>> allSteps = new LinkedList<>();
    for (List<Step<T, D>> list : flattenedList) {
      if (list != null) {
        allSteps.addAll(list);
      }
    }
    return Optional.of(new Itinerary<>(origin,
        allSteps,
        alternatingList.convert(leap -> leap, pathTrial -> Objects.requireNonNull(pathTrial.getPath())),
        length));
  }

}
