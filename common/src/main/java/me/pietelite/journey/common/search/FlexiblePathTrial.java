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
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.Mode;
import me.pietelite.journey.common.navigation.ModeType;
import me.pietelite.journey.common.navigation.Path;
import me.pietelite.journey.common.navigation.Step;
import me.pietelite.journey.common.search.event.StartPathSearchEvent;
import me.pietelite.journey.common.search.event.StepSearchEvent;
import me.pietelite.journey.common.search.event.StopPathSearchEvent;
import me.pietelite.journey.common.search.event.VisitationSearchEvent;
import me.pietelite.journey.common.search.function.ScoringFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An attempt to calculate a {@link Path} encapsulated into an object.
 *
 * @see Path
 * @see SearchSession
 * @see ItineraryTrial
 */
public class FlexiblePathTrial implements Resulted {

  // TODO bring this max size up to something larger like a million but
  //  create a way to identify whether a long-running operation
  //  is likely to be a failure a different way than maxing out memory.
  //  For example, keep track of the best distance so far and if no
  //  significant improvement has been made in a while, then assume it will never.
  public static final int MAX_SIZE = 10000;

  private final SearchSession session;
  @Getter
  private final Cell origin;
  @Getter
  private final String domain;
  @Getter
  private final ScoringFunction scoringFunction;
  private final Completer completer;
  @Getter
  private final List<Mode> modes = new LinkedList<>();
  private final boolean saveOnComplete;
  @Getter
  private double length;
  @Getter
  private Path path;
  @Getter
  private ResultState state;
  @Getter
  private boolean fromCache;
  private long startExecutionTime = -1;

  /**
   * General constructor.
   *
   * @param session         the session requesting this path trial run
   * @param origin          the origin
   * @param scoringFunction the object to score various possibilities when stepping to new locations
   *                        throughout the algorithm
   * @param completer       the object to determine whether the path algorithm is complete and
   *                        the goal has been reached
   */
  public FlexiblePathTrial(SearchSession session,
                           Cell origin,
                           Collection<Mode> modes,
                           ScoringFunction scoringFunction,
                           Completer completer,
                           boolean saveOnComplete) {
    this.session = session;
    this.origin = origin;
    this.domain = origin.domainId();
    this.modes.addAll(modes);
    this.scoringFunction = scoringFunction;
    this.completer = completer;
    this.saveOnComplete = saveOnComplete;
  }

  protected FlexiblePathTrial(SearchSession session,
                              Cell origin,
                              Collection<Mode> modes,
                              ScoringFunction scoringFunction,
                              Completer completer,
                              double length,
                              @Nullable Path path,
                              ResultState state,
                              boolean fromCache,
                              boolean saveOnComplete) {
    this.session = session;
    this.origin = origin;
    this.domain = origin.domainId();
    this.modes.addAll(modes);
    this.scoringFunction = scoringFunction;
    this.completer = completer;
    this.length = length;
    this.path = path;
    this.state = state;
    this.fromCache = fromCache;
    this.saveOnComplete = saveOnComplete;
  }

  private FlexiblePathTrial.TrialResult resultFail() {
    this.state = ResultState.STOPPED_FAILED;
    this.length = Double.MAX_VALUE;
    this.fromCache = false;
    Journey.get().dispatcher().dispatch(new StopPathSearchEvent(session, this, System.currentTimeMillis() - startExecutionTime, saveOnComplete));
    return new TrialResult(null, true);
  }

  private FlexiblePathTrial.TrialResult resultSucceed(double length, List<Step> steps) {
    this.state = ResultState.STOPPED_SUCCESSFUL;
    this.length = length;
    this.path = new Path(origin, new ArrayList<>(steps), length);
    this.fromCache = false;
    Journey.get().dispatcher().dispatch(new StopPathSearchEvent(session, this, System.currentTimeMillis() - startExecutionTime, saveOnComplete));
    return new TrialResult(this.path, true);
  }

  private FlexiblePathTrial.TrialResult resultCancel() {
    this.state = ResultState.STOPPED_CANCELED;
    this.length = Double.MAX_VALUE;
    this.fromCache = false;
    Journey.get().dispatcher().dispatch(new StopPathSearchEvent(session, this, System.currentTimeMillis() - startExecutionTime, false));
    return new TrialResult(null, true);
  }

  /**
   * Attempt to calculate a path given some modes of transportation.
   *
   * @param useCacheIfPossible whether the cache should be used for retrieving previous results
   * @return a result object
   */
  @NotNull
  public TrialResult attempt(boolean useCacheIfPossible) {

    // Return the saved states, but only if we want that result.
    //  If we don't want to use the cache, but this result is from the cache,
    //  then don't return this.
    if (!this.fromCache || useCacheIfPossible) {
      if (this.state == ResultState.STOPPED_SUCCESSFUL) {
        if (path.test(modes)) {
          return new TrialResult(path, false);
        }
      } else if (this.state == ResultState.STOPPED_FAILED) {
        return new TrialResult(null, false);
      }
    }

    // Dispatch a starting event
    Journey.get().dispatcher().dispatch(new StartPathSearchEvent(session, this));
    startExecutionTime = System.currentTimeMillis();

    Queue<Node> upcoming = new PriorityQueue<>(Comparator.comparingDouble(node ->
        -scoringFunction.apply(node.getData().location())));
    Map<Cell, Node> visited = new HashMap<>();

    Node originNode = new Node(new Step(origin, 0, ModeType.NONE),
        null, 0);
    upcoming.add(originNode);
    visited.put(origin, originNode);
    Journey.get().dispatcher().dispatch(new VisitationSearchEvent(session, originNode.getData()));

    Node current;
    while (!upcoming.isEmpty()) {
      if (session.state.get().isCancelFailed()) {
        // Canceled! Fail here, but don't cache it because it's not the true solution for this path.
        return resultCancel();
      }

      if (visited.size() > MAX_SIZE) {
        // We ran out of allocated memory. Let's just call it here and say we failed and cache the failure.
        return resultFail();
      }

      current = upcoming.poll();
      assert current != null;
      Journey.get().dispatcher().dispatch(new StepSearchEvent(session, current.getData()));

      if (completer.test(current)) {
        // We found it!
        double length = current.getScore();
        LinkedList<Step> steps = new LinkedList<>();
        do {
          steps.addFirst(current.getData());
          current = current.getPrevious();
        } while (current != null);
        return resultSucceed(length, steps);
      }

      // Need to keep going
      for (Mode mode : modes) {
        for (Mode.Option option : mode.getDestinations(current.getData().location())) {
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
    }

    // We've exhausted all possibilities. Fail.
    return resultFail();
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
   * A result object to return the result of the {@link #attempt} method.
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