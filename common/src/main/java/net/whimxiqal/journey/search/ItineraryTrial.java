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

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.Value;
import lombok.experimental.Accessors;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.chunk.BlockProvider;
import net.whimxiqal.journey.chunk.SynchronousChunkCache;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.navigation.Path;
import net.whimxiqal.journey.navigation.Step;
import net.whimxiqal.journey.search.event.StartItinerarySearchEvent;
import net.whimxiqal.journey.search.event.StopItinerarySearchEvent;
import net.whimxiqal.journey.tools.AlternatingList;
import org.jetbrains.annotations.NotNull;

/**
 * An attempt to calculate an {@link Itinerary} encapsulated into an object.
 *
 * @see Itinerary
 * @see SearchSession
 * @see PathTrial
 * @see #attempt(boolean)
 */
public class ItineraryTrial implements Resulted {

  private final GraphGoalSearchSession<?> session;
  private final Cell origin;
  private final AlternatingList<Tunnel, PathTrial, Object> alternatingList;
  private final BlockProvider blockProvider;
  private ResultState state;
  private final Object lock = new Object(); // lock for shared objects across multiple path trials
  private boolean changedProblem;
  private int executedPathTrials;

  /**
   * General constructor.
   *
   * @param session         the session
   * @param origin          the origin of the entire itinerary
   * @param alternatingList the list of stages
   */
  public ItineraryTrial(GraphGoalSearchSession<?> session, Cell origin,
                        AlternatingList<Tunnel, PathTrial, Object> alternatingList) {
    this.session = session;
    this.origin = origin;
    this.alternatingList = alternatingList;
    this.blockProvider = new SynchronousChunkCache(session.flags());
    this.state = ResultState.IDLE;
  }

  /**
   * Attempt to calculate an itinerary given some modes of transportation.
   *
   * @param useCacheIfPossible whether the cache should be used for retrieving previous results
   * @return a result object
   */
  @NotNull
  public CompletableFuture<TrialResult> attempt(boolean useCacheIfPossible) {
    CompletableFuture<TrialResult> future = new CompletableFuture<>();
    Journey.get().dispatcher().dispatch(new StartItinerarySearchEvent(session, this));
    state = ResultState.RUNNING;

    List<PathTrial> pathTrialsToExecute = new LinkedList<>();
    for (PathTrial pathTrial : alternatingList.getMinors()) {
      boolean pathTrialDone = false;
      // Use cached value, if appropriate
      if (pathTrial.isFromCache() && useCacheIfPossible) {
        if (pathTrial.getState() == ResultState.STOPPED_SUCCESSFUL) {
          try {
            // TODO this test path uses the block provider and does no sort of preliminary caching.
            //  Since the itinerary trials run on a normal async thread, this logic should be moved to
            //  the path trial execution logic so it's on the designated Journey threads.
            //  And, there should be preliminary caching of chunks since we know what the blocks are we'll be checking.
            if (pathTrial.getPath().test(pathTrial.getModes(), blockProvider)) {
              Journey.logger().info("test succeeded, we don't need to do any path trials");
              pathTrialDone = true;
            }
          } catch (ExecutionException | InterruptedException e) {
            Journey.logger().error(String.format("%s: An %s exception occurred during execution", this, e.getClass().getName()));
            // leave pathTrialDone = false
          }
        } else if (this.state == ResultState.STOPPED_FAILED) {
          pathTrialDone = true;
        }
      }
      if (!pathTrialDone) {
        pathTrialsToExecute.add(pathTrial);
      }
    }

    if (state == ResultState.STOPPED_FAILED) {  // only happens here if we're also only using cached paths
      Journey.get().dispatcher().dispatch(new StopItinerarySearchEvent(session, this));
      future.complete(new TrialResult(Optional.empty(), false));  // doesn't change the problem is caching is used everywhere
      return future;
    }

    if (pathTrialsToExecute.isEmpty()) {
      // everything is cached
      onAllPathTrialsComplete(future);
    }
    for (PathTrial pathTrial : pathTrialsToExecute) {
      Journey.get().workManager().schedule(pathTrial);
      pathTrial.future().thenAccept(pathTrialResult -> {
        synchronized (lock) {
          if (pathTrial.getState() == ResultState.STOPPED_ERROR) {
            state = ResultState.STOPPING_ERROR;
          } else if (pathTrialResult.path().isEmpty()) {
            state = ResultState.STOPPING_FAILED;
          }

          if (pathTrialResult.changedProblem()) {
            changedProblem = true;
          }

          executedPathTrials++;
          if (executedPathTrials < pathTrialsToExecute.size()) {
            return;  // Not all path trials have returned, let's wait until they have
          }
        }
        onAllPathTrialsComplete(future);
      });
    }
    return future;
  }

  private void onAllPathTrialsComplete(CompletableFuture<TrialResult> future) {
    if (state.shouldStop()) {
      state = state.stoppedResult();
    }
    if (state == ResultState.STOPPED_FAILED || state == ResultState.STOPPED_ERROR) {
      Journey.get().dispatcher().dispatch(new StopItinerarySearchEvent(session, this));
      future.complete(new TrialResult(Optional.empty(), changedProblem));
      return;
    }

    // accumulate length
    double length = 0;
    for (Tunnel tunnel : alternatingList.getMajors()) {
      if (tunnel != null) {
        length += tunnel.cost();
      }
    }
    for (PathTrial trial : alternatingList.getMinors()) {
      length += trial.getLength();
    }

    List<List<Step>> flattenedList = alternatingList.flatten(tunnel -> {
      if (tunnel == null) {
        return null;
      } else {
        return Path.fromTunnel(tunnel).getSteps();
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
    future.complete(new TrialResult(Optional.of(new Itinerary(origin,
        allSteps,
        alternatingList.convert(Path::fromTunnel, trial -> Objects.requireNonNull(trial.getPath())),
        length)), changedProblem));
  }

  @Override
  public ResultState getState() {
    return this.state;
  }

  /**
   * A result of a trial attempt.
   *
   * @see #attempt(boolean)
   */
  public record TrialResult(@NotNull Optional<Itinerary> itinerary,
                            boolean changedProblem) {
  }

  @Override
  public String toString() {
    return "ItineraryTrial{" +
        "origin=" + origin +
        ", alternatingList size=" + alternatingList.size() +
        ", state=" + state +
        ", changedProblem=" + changedProblem +
        ", executedPathTrials=" + executedPathTrials +
        '}';
  }
}
