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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.chunk.ChunkCacheBlockProvider;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.manager.WorkItem;
import net.whimxiqal.journey.navigation.Mode;
import net.whimxiqal.journey.navigation.ModeType;
import net.whimxiqal.journey.navigation.Path;
import net.whimxiqal.journey.navigation.Step;
import net.whimxiqal.journey.search.event.StartPathSearchEvent;
import net.whimxiqal.journey.search.event.StepSearchEvent;
import net.whimxiqal.journey.search.event.StopPathSearchEvent;
import net.whimxiqal.journey.search.event.VisitationSearchEvent;
import net.whimxiqal.journey.search.function.CostFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An attempt to calculate a {@link Path} encapsulated into an object.
 *
 * @see Path
 * @see SearchSession
 * @see ItineraryTrial
 */
public class AbstractPathTrial implements WorkItem, Resulted {

  /**
   * How many cells to run per cycle.
   * The fewer the cells, the more cycles this has to run to complete the search,
   * but the more opportunities other searches get to override this to make room for other necessary work.
   * Left un-final for testing.
   */
  public static double CELLS_PER_EXECUTION_CYCLE = 1000;

  /**
   * How many chunks we want to cache during
   */
  public static final int MAX_CACHED_CHUNKS_PER_SEARCH = 128;
  @Getter
  protected final Cell origin;
  protected final ChunkCacheBlockProvider chunkCache;
  protected final Queue<Node> upcoming;
  private final SearchSession session;
  @Getter
  private final int domain;
  @Getter
  private final CostFunction costFunction;
  private final CostFunction secondaryCostFunction;
  private final Completer completer;
  @Getter
  private final List<Mode> modes = new LinkedList<>();
  private final boolean saveOnComplete;
  private final CompletableFuture<TrialResult> future = new CompletableFuture<>();
  private final Map<Cell, Node> visited = new HashMap<>();
  protected long startExecutionTime = -1;
  @Getter
  private double length;
  @Getter
  private Path path;
  @Getter
  private ResultState state;
  @Getter
  private boolean fromCache;
  private int maxCellCount = Settings.MAX_PATH_BLOCK_COUNT.getValue();
  // Search State
  private boolean firstCycle = true;
  private long nextAllowedRunTime = 0;

  /**
   * General constructor.
   *
   * @param session               the session requesting this path trial run
   * @param origin                the origin
   * @param costFunction          the object to score various possibilities when stepping to new locations
   *                              throughout the algorithm
   * @param secondaryCostFunction a secondary function to evaluate after the cost function
   * @param completer             the object to determine whether the path algorithm is complete and
   *                              the goal has been reached
   */
  public AbstractPathTrial(SearchSession session,
                           Cell origin,
                           Collection<Mode> modes,
                           CostFunction costFunction,
                           CostFunction secondaryCostFunction,
                           Completer completer,
                           boolean saveOnComplete) {
    this(session, origin, modes, costFunction, secondaryCostFunction, completer, 0, null, ResultState.IDLE, false, saveOnComplete);
  }

  protected AbstractPathTrial(SearchSession session,
                              Cell origin,
                              Collection<Mode> modes,
                              CostFunction costFunction,
                              CostFunction secondaryCostFunction,
                              Completer completer,
                              double length,
                              @Nullable Path path,
                              ResultState state,
                              boolean fromCache,
                              boolean saveOnComplete) {
    this.session = session;
    this.origin = origin;
    this.domain = origin.domain();
    this.modes.addAll(modes);
    this.costFunction = costFunction;
    this.secondaryCostFunction = secondaryCostFunction;
    this.completer = completer;
    this.length = length;
    this.path = path;
    this.state = state;
    this.fromCache = fromCache;
    this.saveOnComplete = saveOnComplete;
    this.chunkCache = new ChunkCacheBlockProvider(MAX_CACHED_CHUNKS_PER_SEARCH, session.flags());
    this.upcoming = new PriorityQueue<>(Comparator.<Node>comparingDouble(node -> node.score + costFunction.apply(node.data.location()))
        .thenComparingDouble(node -> secondaryCostFunction.apply(node.data.location())));
  }

  private void resultFail() {
    this.state = ResultState.STOPPED_FAILED;
    this.length = Double.MAX_VALUE;
    this.fromCache = false;
    Journey.get().dispatcher().dispatch(new StopPathSearchEvent(session, this, System.currentTimeMillis() - startExecutionTime, saveOnComplete));
    future.complete(new TrialResult(null, true));
  }

  private void resultSucceed(double length, List<Step> steps) {
    this.state = ResultState.STOPPED_SUCCESSFUL;
    this.length = length;
    this.path = new Path(origin, new ArrayList<>(steps), length);
    this.fromCache = false;
    if (saveOnComplete) {
      cacheSuccess();
    }
    Journey.get().dispatcher().dispatch(new StopPathSearchEvent(session, this, System.currentTimeMillis() - startExecutionTime, saveOnComplete));
    future.complete(new TrialResult(this.path, true));
  }

  private void resultCancel() {
    this.state = ResultState.STOPPED_CANCELED;
    this.length = Double.MAX_VALUE;
    this.fromCache = false;
    Journey.get().dispatcher().dispatch(new StopPathSearchEvent(session, this, System.currentTimeMillis() - startExecutionTime, false));
    future.complete(new TrialResult(null, true));
  }

  private void resultError() {
    this.state = ResultState.STOPPED_ERROR;
    this.length = Double.MAX_VALUE;
    this.fromCache = false;
    Journey.get().dispatcher().dispatch(new StopPathSearchEvent(session, this, System.currentTimeMillis() - startExecutionTime, false));
    future.complete(new TrialResult(null, true));
  }

  @Override
  public void reset() {
    firstCycle = true;
    upcoming.clear();
    visited.clear();
    state = ResultState.IDLE;
  }

  protected void prepareIo() {
    // nothing by default
  }

  /**
   * Attempt to calculate a path given some modes of transportation.
   */
  @Override
  public boolean run() {
    try {
      return runSafe();
    } catch (Exception e) {
      e.printStackTrace();
      resultError();
      return true;
    }
  }

  private boolean runSafe() {
    if (state.isStopped()) {
      return true;
    }
    // Dispatch a starting event
    if (state == ResultState.IDLE) {
      Journey.get().dispatcher().dispatch(new StartPathSearchEvent(session, this));
      startExecutionTime = System.currentTimeMillis();
      state = ResultState.RUNNING;
    }

    if (firstCycle) {
      Node originNode = new Node(new Step(origin, 0, ModeType.NONE),
          null, 0);
      upcoming.add(originNode);
      visited.put(origin, originNode);
      Journey.get().dispatcher().dispatch(new VisitationSearchEvent(session, originNode.getData()));
      firstCycle = false;
    }

    if (session.getAlgorithmStepDelay() != 0) {
      // We need to track our times that we run so that we can delay appropriately
      long now = System.currentTimeMillis();
      if (nextAllowedRunTime > now) {
        // we cannot run now, wait for next run
        return false;
      } else {
        // We can run since we're past the allowed run time, but set the next one
        nextAllowedRunTime = now + session.getAlgorithmStepDelay();
      }
    }

    // Start actual execution
    int startingCycleCount = visited.size();  // tracker to make sure we have short work cycles

    Node current;
    while (!upcoming.isEmpty()) {
      // Queue any IO we will likely needed
      prepareIo();

      synchronized (session) {
        if (session.state.shouldStop()) {
          // Canceled! Fail here, but don't cache it because it's not the true solution for this path.
          resultCancel();
          return true;
        }
      }

      if (visited.size() > maxCellCount) {
        // We ran out of allocated memory. Let's just call it here and say we failed and cache the failure.
        resultFail();
        return true;
      }

      if (visited.size() >= startingCycleCount + CELLS_PER_EXECUTION_CYCLE) {
        // Quit after a certain number of blocks to allow other searches to run
        return false;  // (not done)
      }

      current = upcoming.poll();
      assert current != null;
      Journey.get().dispatcher().dispatch(new StepSearchEvent(session, current.getData()));

      if (completer.test(current)) {
        // We found it!
        double length = current.getScore();
        completer.test(current);
        LinkedList<Step> steps = new LinkedList<>();
        do {
          steps.addFirst(current.getData());
          current = current.getPrevious();
        } while (current != null);
        resultSucceed(length, steps);
        return true;
      }

      // Need to keep going
      for (Mode mode : modes) {
        Collection<Mode.Option> options;
        try {
          options = mode.getDestinations(current.getData().location(), chunkCache);
        } catch (ExecutionException | InterruptedException e) {
          Journey.logger().error(String.format("An %s occurred during the following path trial: %s", e.getClass().getName(), this));
          continue;
        }
        for (Mode.Option option : options) {
          if (visited.containsKey(option.location())) {
            // Already visited, but see if it is better to come from this new direction
            Node that = visited.get(option.location());
            if (current.getScore() + option.cost() < that.getScore()) {
              that.setPrevious(current);
              that.setScore(current.getScore() + option.cost());
              that.setData(new Step(that.getData().location(),
                  option.cost(),
                  mode.type()));
            }
          } else {
            // Not visited. Set up node, give it a score, and add it to the system
            Node nextNode = new Node(
                new Step(option.location(),
                    option.cost(),
                    mode.type()),
                current,
                current.getScore() + option.cost());
            upcoming.add(nextNode);
            visited.put(option.location(), nextNode);
            Journey.get().dispatcher().dispatch(new VisitationSearchEvent(session, nextNode.getData()));
          }
        }
      }
      if (session.getAlgorithmStepDelay() != 0) {
        // delay the algorithm per the request of the session (animation purposes)
        return false;
      }
    }

    // We've exhausted all possibilities. Fail.
    resultFail();
    return true;
  }

  protected void cacheSuccess() {
    // do nothing by default
  }

  public void setMaxCellCount(int maxCellCount) {
    this.maxCellCount = maxCellCount;
  }

  @Override
  public UUID owner() {
    // "Owner" is just the session id
    return session.uuid();
  }

  @Override
  public String toString() {
    return "PathTrial{state: " + state
        + ", origin:" + origin
        + ", costFunction:" + costFunction
        + ", secondaryCostFunction:" + secondaryCostFunction
        + ", fromCache:" + fromCache
        + "}";
  }

  public CompletableFuture<TrialResult> future() {
    return future;
  }

  /**
   * An interface to represent when a node is considered successful and therefore
   * the end of a successful path.
   * At this point in the algorithm, the path up until and through this node is returned.
   */
  @FunctionalInterface
  public interface Completer extends Predicate<Node> {
  }

  /**
   * A result object to return the result of the overall search.
   */
  public static class TrialResult {
    Path path;
    boolean changedProblem;

    TrialResult(Path path, boolean changedProblem) {
      this.path = path;
      this.changedProblem = changedProblem;
    }

    Optional<Path> path() {
      return Optional.ofNullable(path);
    }

    boolean changedProblem() {
      return changedProblem;
    }
  }

  /**
   * A single node representing a possible movement during traversal.
   */
  public static class Node {
    @Getter
    @Setter
    private Step data;
    @Getter
    @Setter
    private Node previous;
    /**
     * The value to store how far away this node is from the original node.
     * So, how far it is to traverse the space from the origin until this node is reached.
     */
    @Getter
    @Setter
    private double score;

    /**
     * General constructor.
     *
     * @param data     the step
     * @param previous the previous node that we came from to get here
     * @param score    our score so far throughout the pathfinding algorithm
     */
    public Node(@NotNull Step data, Node previous, double score) {
      this.data = data;
      this.previous = previous;
      this.score = score;
    }

  }


}