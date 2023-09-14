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
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.chunk.BlockProvider;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.navigation.Path;
import net.whimxiqal.journey.search.function.HeightCostFunction;
import net.whimxiqal.journey.tools.AlternatingList;

/**
 * A search session that's purpose is to run a search, trying to find
 * a location that reaches some criteria by moving upwards as much as possible.
 */
public class SurfaceGoalSearchSession extends SearchSession {

  private final Cell origin;

  public SurfaceGoalSearchSession(UUID callerId, Caller callerType, JourneyAgent agent, Cell origin) {
    super(callerId, callerType, agent);
    this.origin = origin;
  }

  @Override
  public void asyncSearch() {
    // This implementation just runs once
    super.timer.start();
    state.set(ResultState.RUNNING);

    PathTrial pathTrial = new PathTrial(
        this,
        origin,
        modes(),
        new HeightCostFunction(),
        (blockProvider, node) -> BlockProvider.isAtSurface(blockProvider, node.getData().location()),
        false
    );

    Journey.get().workManager().schedule(pathTrial);

    pathTrial.future().thenAccept(this::onPathTrialComplete);
  }

  /**
   * Callback when the path trial finishes.
   *
   * @param pathTrialResult the result of a path trial
   */
  private void onPathTrialComplete(PathTrial.TrialResult pathTrialResult) {
    ResultState updated = state.updateAndGet(current -> {
      if (current.shouldStop()) {
        return current.stoppedResult();
      }
      return current;
    });

    // this was stopped while attempting the path trial
    if (updated.isStopped()) {
      complete(null);
      return;
    }

    // Otherwise, we just assume the same result as the (single) path trial
    state.set(pathTrialResult.state());

    if (pathTrialResult.path() == null) {
      complete(null);
    } else {
      AlternatingList.Builder<Path, Path, Path> stages
          = AlternatingList.builder(Path.stationary(origin));
      stages.addLast(pathTrialResult.path(), Path.stationary(pathTrialResult.path().getDestination()));
      complete(new Itinerary(this.origin,
          pathTrialResult.path().getSteps(),
          pathTrialResult.path().getCost()));
    }
  }

}