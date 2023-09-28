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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.chunk.BlockProvider;
import net.whimxiqal.journey.chunk.ChunkCacheBlockProvider;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.navigation.Path;
import net.whimxiqal.journey.navigation.Step;
import net.whimxiqal.journey.search.flag.FlagSet;
import net.whimxiqal.journey.tools.AlternatingList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An attempt to calculate an {@link Itinerary} encapsulated into an object.
 *
 * @see Itinerary
 * @see SearchSession
 * @see DestinationPathTrial
 * @see #attempt(boolean)
 */
public class ItineraryTrial {

  private final SearchSession session;
  private final Cell origin;
  private final AlternatingList<Tunnel, DestinationPathTrial, Object> alternatingList;
  private final BlockProvider blockProvider;
  private final AtomicReference<ResultState> state;
  private boolean changedProblem;
  private int executedPathTrials;

  /**
   * General constructor.
   *
   * @param origin          the origin of the entire itinerary
   * @param alternatingList the list of stages
   */
  public ItineraryTrial(SearchSession session, Cell origin, AlternatingList<Tunnel, DestinationPathTrial, Object> alternatingList, FlagSet flags) {
    this.session = session;
    this.origin = origin;
    this.alternatingList = alternatingList;
    this.blockProvider = new ChunkCacheBlockProvider(PathTrial.MAX_CACHED_CHUNKS_PER_SEARCH, flags);
    this.state = new AtomicReference<>(ResultState.IDLE);
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
    Journey.logger().debug(this + ": itinerary trial started");
    state.set(ResultState.RUNNING);

    List<DestinationPathTrial> pathTrialsToExecute = new LinkedList<>();
    for (DestinationPathTrial pathTrial : alternatingList.getMinors()) {
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
              pathTrialDone = true;
            }
          } catch (ExecutionException | InterruptedException e) {
            Journey.logger().error(String.format("%s: An %s exception occurred during execution", this, e.getClass().getName()));
            // leave pathTrialDone = false
          }
        } else if (state.get() == ResultState.STOPPED_FAILED) {
          pathTrialDone = true;
        }
      }
      if (!pathTrialDone) {
        pathTrialsToExecute.add(pathTrial);
      }
    }

    if (state.get() == ResultState.STOPPED_FAILED) {  // only happens here if we're also only using cached paths
      Journey.logger().info(this + ": itinerary failed before path trials ran");
      future.complete(new TrialResult(ResultState.STOPPED_FAILED, null, false));  // doesn't change the problem is caching is used everywhere
      return future;
    }

    Journey.logger().debug(String.format("%s: itinerary trial needs to calculate %d paths", this, pathTrialsToExecute.size()));
    if (pathTrialsToExecute.isEmpty()) {
      // everything is cached
      onPathTrialComplete(null, 0, future);
    } else {
      for (DestinationPathTrial pathTrial : pathTrialsToExecute) {
        Journey.get().workManager().schedule(pathTrial);
        pathTrial.future().thenAccept(pathTrialResult -> onPathTrialComplete(pathTrialResult, pathTrialsToExecute.size(), future));
      }
    }
    return future;
  }

  private synchronized void onPathTrialComplete(PathTrial.TrialResult result, int total, CompletableFuture<TrialResult> future) {
    if (result != null) {  // only null if no path trial was run at all
      state.updateAndGet(current -> {
        // set in order of precedence: error, canceled, failed
        if (result.state() == ResultState.STOPPED_ERROR) {
          Journey.logger().debug(this + ": found path trial with error result, setting same");
          return ResultState.STOPPING_ERROR;
        } else if (result.state() == ResultState.STOPPED_CANCELED) {
          if (current == ResultState.STOPPING_ERROR) {
            return current;
          } else {
            Journey.logger().debug(this + ": found path trial with canceled result, setting same");
            return ResultState.STOPPING_CANCELED;
          }
        } else if (result.state() == ResultState.STOPPED_FAILED) {
          if (current == ResultState.STOPPING_ERROR || current == ResultState.STOPPING_CANCELED) {
            return current;
          } else {
            Journey.logger().debug(this + ": found path trial with failed result, setting same");
            return ResultState.STOPPING_FAILED;
          }
        }
        return current;  // just keep it the same
      });

      if (result.changedProblem()) {
        changedProblem = true;
      }
    }

    executedPathTrials++;
    if (executedPathTrials < total) {
      return;  // Not all path trials have returned, let's wait until they have
    }

// check if we need to stop
    ResultState newState = state.updateAndGet(current -> {
      if (current.shouldStop()) {
        return current.stoppedResult();
      }
      return current;
    });

    if (newState.isStopped()) {
      Journey.logger().debug(this + ": itinerary trial stopped");
      future.complete(new TrialResult(newState, null, changedProblem));
      return;
    }

    // accumulate length
    double length = 0;
    for (Tunnel tunnel : alternatingList.getMajors()) {
      if (tunnel != null) {
        length += tunnel.cost();
      }
    }
    for (DestinationPathTrial trial : alternatingList.getMinors()) {
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
    state.set(ResultState.STOPPED_SUCCESSFUL);
    Journey.logger().debug(this + ": itinerary trial succeeded");
    future.complete(new TrialResult(state.get(),
        new Itinerary(origin,
            allSteps,
            length),
        changedProblem));
  }

  @Override
  public String toString() {
    return "[Itinerary Search] {session: " + session.uuid
        + ", origin: " + origin
        + ", paths: " + alternatingList.getMinors().size()
        + ", path searches: " + executedPathTrials
        + ", state: " + state
        + ", changed graph: " + changedProblem
        + '}';
  }

  /**
   * A result of a trial attempt.
   *
   * @see #attempt(boolean)
   */
  public record TrialResult(ResultState state,
                            @Nullable Itinerary itinerary,
                            boolean changedProblem) {
  }
}
