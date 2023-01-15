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

package net.whimxiqal.journey.common.search;

import java.util.UUID;
import net.whimxiqal.journey.common.Journey;
import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.journey.common.navigation.Itinerary;
import net.whimxiqal.journey.common.navigation.Path;
import net.whimxiqal.journey.common.navigation.Port;
import net.whimxiqal.journey.common.search.event.FoundSolutionEvent;
import net.whimxiqal.journey.common.search.event.StartSearchEvent;
import net.whimxiqal.journey.common.search.event.StopSearchEvent;
import net.whimxiqal.journey.common.search.flag.FlagSet;
import net.whimxiqal.journey.common.search.function.HeightCostFunction;
import net.whimxiqal.journey.common.tools.AlternatingList;

/**
 * A search session that's purpose is to run a search, trying to find
 * a location that reaches some criteria by moving upwards as much as possible.
 */
public abstract class LocalUpwardsGoalSearchSession extends SearchSession {

  private final Cell origin;
  private long executionStartTime = -1;

  protected LocalUpwardsGoalSearchSession(UUID callerId, Caller callerType, Cell origin, FlagSet flags) {
    super(callerId, callerType, flags);
    this.origin = origin;
  }

  @Override
  protected void resumeSearch() {
    // This implementation just runs once
    executionStartTime = System.currentTimeMillis();
    Journey.get().dispatcher().dispatch(new StartSearchEvent(this));
    state.set(ResultState.RUNNING);

    FlexiblePathTrial pathTrial = new FlexiblePathTrial(
        this,
        origin,
        this.modes,
        new HeightCostFunction(),
        node -> reachesGoal(node.getData().location()),
        false
    );

    PathTrial.TrialResult result = pathTrial.attempt(false);

    if (result.path().isPresent()) {
      AlternatingList.Builder<Port, Path, Path> stages
          = AlternatingList.builder(Port.stationary(origin));
      stages.addLast(result.path().get(), Port.stationary(result.path().get().getDestination()));

      Journey.get().dispatcher().dispatch(new FoundSolutionEvent(
          this,
          new Itinerary(this.origin,
              result.path().get().getSteps(),
              stages.build(),
              result.path().get().getCost())));
    }

    state.set(pathTrial.getState());
    Journey.get().dispatcher().dispatch(new StopSearchEvent(this));

  }

  @Override
  public final long executionTime() {
    if (executionStartTime < 0) {
      return -1;
    }
    return System.currentTimeMillis() - executionStartTime;
  }

  protected abstract boolean reachesGoal(Cell cell);

}