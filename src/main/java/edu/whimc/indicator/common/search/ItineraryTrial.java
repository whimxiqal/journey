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

import edu.whimc.indicator.common.IndicatorCommon;
import edu.whimc.indicator.common.navigation.Cell;
import edu.whimc.indicator.common.navigation.Itinerary;
import edu.whimc.indicator.common.navigation.Leap;
import edu.whimc.indicator.common.navigation.Mode;
import edu.whimc.indicator.common.navigation.Path;
import edu.whimc.indicator.common.navigation.Step;
import edu.whimc.indicator.common.search.event.StartItinerarySearchEvent;
import edu.whimc.indicator.common.search.event.StopItinerarySearchEvent;
import edu.whimc.indicator.common.tools.AlternatingList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class ItineraryTrial<T extends Cell<T, D>, D> implements Resulted {

  private final SearchSession<T, D> session;
  private final T origin;
  private final AlternatingList<Leap<T, D>, PathTrial<T, D>, Object> alternatingList;
  private ResultState state;

  public ItineraryTrial(SearchSession<T, D> session, T origin,
                        AlternatingList<Leap<T, D>, PathTrial<T, D>, Object> alternatingList) {
    this.session = session;
    this.origin = origin;
    this.alternatingList = alternatingList;
    this.state = ResultState.IDLE;
  }

  @NotNull
  public TrialResult<T, D> attempt(Collection<Mode<T, D>> modes, boolean useCache) {
    IndicatorCommon.<T, D>getSearchEventDispatcher().dispatch(new StartItinerarySearchEvent<>(session, this));

    state = ResultState.RUNNING;
    boolean failed = false;
    boolean changedProblem = false;
    for (PathTrial<T, D> pathTrial : alternatingList.getMinors()) {
      PathTrial.TrialResult<T, D> pathTrialResult = pathTrial.attempt(modes, useCache);
      if (pathTrialResult.changedProblem()) {
        changedProblem = true;
      }
      if (pathTrialResult.path().isEmpty()) {
        failed = true;
      }
    }
    if (failed) {
      state = ResultState.FAILED;
      IndicatorCommon.<T, D>getSearchEventDispatcher().dispatch(new StopItinerarySearchEvent<>(session, this));
      return new TrialResult<>(Optional.empty(), changedProblem);
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
    state = ResultState.SUCCESSFUL;
    IndicatorCommon.<T, D>getSearchEventDispatcher().dispatch(new StopItinerarySearchEvent<>(session, this));
    return new TrialResult<>(Optional.of(new Itinerary<>(origin,
        allSteps,
        alternatingList.convert(leap -> leap, pathTrial -> Objects.requireNonNull(pathTrial.getPath())),
        length)), changedProblem);
  }

  @Override
  public ResultState getState() {
    return this.state;
  }

  public static final record TrialResult<T extends Cell<T, D>, D>(Optional<Itinerary<T, D>> itinerary,
                                                                  boolean changedProblem) { }

}
