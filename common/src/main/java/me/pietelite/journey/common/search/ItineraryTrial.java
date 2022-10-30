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

package me.pietelite.journey.common.search;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Value;
import lombok.experimental.Accessors;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.Itinerary;
import me.pietelite.journey.common.navigation.Port;
import me.pietelite.journey.common.navigation.Step;
import me.pietelite.journey.common.search.event.StartItinerarySearchEvent;
import me.pietelite.journey.common.search.event.StopItinerarySearchEvent;
import me.pietelite.journey.common.tools.AlternatingList;
import org.jetbrains.annotations.NotNull;

/**
 * An attempt to calculate an {@link Itinerary} encapsulated into an object.
 * @see Itinerary
 * @see SearchSession
 * @see PathTrial
 * @see #attempt(boolean)
 */
public class ItineraryTrial implements Resulted {

  private final SearchSession session;
  private final Cell origin;
  private final AlternatingList<Port, PathTrial, Object> alternatingList;
  private ResultState state;

  /**
   * General constructor.
   *
   * @param session         the session
   * @param origin          the origin of the entire itinerary
   * @param alternatingList the list of stages
   */
  public ItineraryTrial(SearchSession session, Cell origin,
                        AlternatingList<Port, PathTrial, Object> alternatingList) {
    this.session = session;
    this.origin = origin;
    this.alternatingList = alternatingList;
    this.state = ResultState.IDLE;
  }

  /**
   * Attempt to calculate an itinerary given some modes of transportation.
   *
   * @param useCacheIfPossible whether the cache should be used for retrieving previous results
   * @return a result object
   */
  @NotNull
  public TrialResult attempt(boolean useCacheIfPossible) {
    Journey.get().dispatcher().dispatch(new StartItinerarySearchEvent(session, this));

    state = ResultState.RUNNING;
    boolean failed = false;
    boolean changedProblem = false;
    for (PathTrial pathTrial : alternatingList.getMinors()) {

      if (session.state.get().shouldStop()) {
        // officially stop
        state = state.stoppedResult();
        Journey.get().dispatcher().dispatch(
            new StopItinerarySearchEvent(session, this));
        return new TrialResult(Optional.empty(), true);  // doesn't really matter if changed problem
      }

      PathTrial.TrialResult pathTrialResult = pathTrial.attempt(useCacheIfPossible);
      if (pathTrialResult.changedProblem()) {
        changedProblem = true;
      }
      if (!pathTrialResult.path().isPresent()) {
        failed = true;
      }
    }
    if (failed) {
      state = ResultState.STOPPED_FAILED;
      Journey.get().dispatcher().dispatch(new StopItinerarySearchEvent(session, this));
      return new TrialResult(Optional.empty(), changedProblem);
    }

    // accumulate length
    double length = 0;
    for (Port port : alternatingList.getMajors()) {
      if (port != null) {
        length += port.getCost();
      }
    }
    for (PathTrial pathTrial : alternatingList.getMinors()) {
      length += pathTrial.getLength();
    }

    List<List<Step>> flattenedList = alternatingList.flatten(leap -> {
      if (leap == null) {
        return null;
      } else {
        return leap.getSteps();
      }
    }, trial -> trial.getPath().getSteps() /* Path must exist because we didn't fail */);
    List<Step> allSteps = new LinkedList<>();
    for (List<Step> list : flattenedList) {
      if (list != null) {
        allSteps.addAll(list);
      }
    }
    state = ResultState.STOPPED_SUCCESSFUL;
    Journey.get().dispatcher().dispatch(new StopItinerarySearchEvent(session, this));
    return new TrialResult(Optional.of(new Itinerary(origin,
        allSteps,
        alternatingList.convert(leap -> leap, pathTrial -> Objects.requireNonNull(pathTrial.getPath())),
        length)), changedProblem);
  }

  @Override
  public ResultState getState() {
    return this.state;
  }

  /**
   * A result of a trial attempt.
   * @see #attempt(boolean)
   */
  @Value
  @Accessors(fluent = true)
  public static class TrialResult {
    @NotNull Optional<Itinerary> itinerary;
    boolean changedProblem;
  }

}
