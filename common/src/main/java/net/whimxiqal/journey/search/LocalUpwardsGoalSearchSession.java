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

package net.whimxiqal.journey.search;

import java.util.UUID;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.navigation.Path;
import net.whimxiqal.journey.search.event.FoundSolutionEvent;
import net.whimxiqal.journey.search.event.StartSearchEvent;
import net.whimxiqal.journey.search.event.StopSearchEvent;
import net.whimxiqal.journey.search.function.HeightCostFunction;
import net.whimxiqal.journey.tools.AlternatingList;

/**
 * A search session that's purpose is to run a search, trying to find
 * a location that reaches some criteria by moving upwards as much as possible.
 */
public abstract class LocalUpwardsGoalSearchSession extends SearchSession {

  private final Cell origin;

  protected LocalUpwardsGoalSearchSession(UUID callerId, Caller callerType, Cell origin) {
    super(callerId, callerType);
    this.origin = origin;
  }

  @Override
  protected void resumeSearch() {
    // This implementation just runs once
    super.timer.start();
    Journey.get().dispatcher().dispatch(new StartSearchEvent(this));
    synchronized (this) {
      state = ResultState.RUNNING;
    }

    AbstractPathTrial pathTrial = new AbstractPathTrial(
        this,
        origin,
        this.modes,
        new HeightCostFunction(),
        node -> reachesGoal(node.getData().location()),
        false
    );

    PathTrial.TrialResult result = pathTrial.attempt(false);

    synchronized (this) {
      if (state.shouldStop()) {
        // this was stopped while attempting the path trial
        state = state.stoppedResult();
        Journey.get().dispatcher().dispatch(new StopSearchEvent(this));
        return;
      } else {
        state = pathTrial.getState();
      }
    }

    if (result.path().isPresent()) {
      AlternatingList.Builder<Path, Path, Path> stages
          = AlternatingList.builder(Path.stationary(origin));
      stages.addLast(result.path().get(), Path.stationary(result.path().get().getDestination()));

      Journey.get().dispatcher().dispatch(new FoundSolutionEvent(
          this,
          new Itinerary(this.origin,
              result.path().get().getSteps(),
              stages.build(),
              result.path().get().getCost())));
    }

    Journey.get().dispatcher().dispatch(new StopSearchEvent(this));
  }

  protected abstract boolean reachesGoal(Cell cell);

}