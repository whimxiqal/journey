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
import edu.whimc.indicator.common.navigation.Mode;
import edu.whimc.indicator.common.navigation.ModeType;
import edu.whimc.indicator.common.navigation.ModeTypeGroup;
import edu.whimc.indicator.common.navigation.Path;
import edu.whimc.indicator.common.navigation.Step;
import edu.whimc.indicator.common.search.event.StartPathSearchEvent;
import edu.whimc.indicator.common.search.event.StepSearchEvent;
import edu.whimc.indicator.common.search.event.StopPathSearchEvent;
import edu.whimc.indicator.common.search.event.VisitationSearchEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class PathTrial<T extends Cell<T, D>, D> implements Resulted {

  // TODO bring this max size up to something larger like a million but
  //  create a way to identify whether a long-running operation
  //  is likely to be a failure a different way than maxing out memory.
  //  For example, keep track of the best distance so far and if no
  //  significant improvement has been made in a while, then assume it will never.
  public static final int MAX_SIZE = 10000;
  public static final double SUFFICIENT_COMPLETION_DISTANCE_SQUARED = 0;

  private final SearchSession<T, D> session;
  @Getter
  private final T origin;
  @Getter
  private final T destination;
  @Getter
  private final D domain;
  @Getter
  private double length;
  @Getter
  private Path<T, D> path;
  @Getter
  private ResultState state;
  @Getter
  private final boolean fromCache;

  private PathTrial(SearchSession<T, D> session,
                    T origin, T destination,
                    double length,
                    Path<T, D> path,
                    ResultState state,
                    boolean fromCache) {
    if (!origin.getDomain().equals(destination.getDomain())) {
      throw new IllegalArgumentException("The domain of the origin and destination must be the same");
    }
    this.session = session;
    this.origin = origin;
    this.destination = destination;
    this.domain = origin.getDomain();
    this.length = length;
    this.path = path;
    this.state = state;
    this.fromCache = fromCache;
  }

  public static <T extends Cell<T, D>, D> PathTrial<T, D> successful(SearchSession<T, D> session,
                                                                     T origin, T destination,
                                                                     Path<T, D> path) {
    return new PathTrial<>(session, origin, destination,
        path.getLength(), path,
        ResultState.SUCCESSFUL, false);
  }

  public static <T extends Cell<T, D>, D> PathTrial<T, D> failed(SearchSession<T, D> session,
                                                                 T origin, T destination) {
    return new PathTrial<>(session, origin, destination,
        Double.MAX_VALUE, null,
        ResultState.FAILED, false);
  }

  public static <T extends Cell<T, D>, D> PathTrial<T, D> approximate(SearchSession<T, D> session,
                                                                      T origin, T destination) {
    return new PathTrial<>(session, origin, destination,
        origin.distanceTo(destination), null,
        ResultState.IDLE, false);
  }

  public static <T extends Cell<T, D>, D> PathTrial<T, D> cached(SearchSession<T, D> session,
                                                                 T origin, T destination,
                                                                 Path<T, D> path) {
    return new PathTrial<>(session, origin, destination,
        path == null ? origin.distanceTo(destination) : path.getLength(), path,
        path == null ? ResultState.FAILED : ResultState.SUCCESSFUL,
        true);
  }

  @NotNull
  public TrialResult<T, D> attempt(Collection<Mode<T, D>> modes, boolean useCacheIfPossible) {
    if (!origin.getDomain().equals(destination.getDomain())) {
      throw new IllegalArgumentException("The input locatables ["
          + origin + " and "
          + destination
          + "] must have the same domain to search for a path");
    }

    // Return the saved states, but only if we want that result.
    //  If we don't want to use the cache, but this result is from the cache,
    //  then don't return this.
    if (this.state == ResultState.SUCCESSFUL && (useCacheIfPossible || !this.fromCache)) {
      return new TrialResult<>(Optional.of(path), false);
    }

    if (this.state == ResultState.FAILED && (useCacheIfPossible || !this.fromCache)) {
      return new TrialResult<>(Optional.empty(), false);
    }

    // Dispatch a starting event
    IndicatorCommon.<T, D>getSearchEventDispatcher().dispatch(new StartPathSearchEvent<>(session, this));

    Queue<Node> upcoming = new PriorityQueue<>(Comparator.comparingDouble(Node::getProximity));
    Map<T, Node> visited = new HashMap<>();

    Node originNode = new Node(new Step<>(origin, ModeType.NONE),
        null,
        origin.distanceTo(destination), 0);
    upcoming.add(originNode);
    visited.put(origin, originNode);
    IndicatorCommon.<T, D>getSearchEventDispatcher().dispatch(new VisitationSearchEvent<>(session, originNode.getData()));

    Node current;
    while (!upcoming.isEmpty()) {

      if (session.state.isCanceled()) {
        // Canceled! Fail here, but don't cache it because it's not the true solution for this path.
        this.state = ResultState.FAILED;
        this.length = Double.MAX_VALUE;
        return new TrialResult<>(Optional.empty(), true);
      }

      if (visited.size() > MAX_SIZE) {
        // We ran out of allocated memory. Let's just call it here and say we failed and cache the failure.
        this.state = ResultState.FAILED;
        this.length = Double.MAX_VALUE;
        IndicatorCommon.<T, D>getPathCache().put(origin, destination, ModeTypeGroup.from(modes), null);
        return new TrialResult<>(Optional.empty(), true);
      }

      current = upcoming.poll();
      assert current != null;
      IndicatorCommon.<T, D>getSearchEventDispatcher().dispatch(new StepSearchEvent<>(session, originNode.getData()));

      if (current.getData().getLocatable().distanceToSquared(destination) <= SUFFICIENT_COMPLETION_DISTANCE_SQUARED) {
        // We found it!
        double length = current.getScore();
        LinkedList<Step<T, D>> steps = new LinkedList<>();
        do {
          steps.addFirst(current.getData());
          current = current.getPrevious();
        } while (current != null);
        this.state = ResultState.SUCCESSFUL;
        this.length = length;
        this.path = new Path<>(origin, new ArrayList<>(steps), length);
        IndicatorCommon.<T, D>getSearchEventDispatcher().dispatch(new StopPathSearchEvent<>(session, this));
        IndicatorCommon.<T, D>getPathCache().put(origin, destination, ModeTypeGroup.from(modes), this.path);
        return new TrialResult<>(Optional.of(this.path), true);
      }

      // Need to keep going
      for (Mode<T, D> mode : modes) {
        for (Map.Entry<T, Double> next : mode.getDestinations(current.getData().getLocatable(), session).entrySet()) {
          if (visited.containsKey(next.getKey())) {
            // Already visited, but see if it is better to come from this new direction
            Node that = visited.get(next.getKey());
            if (current.getScore() + next.getValue() < that.getScore()) {
              that.setPrevious(current);
              that.setScore(current.getScore() + next.getValue());
              that.setData(new Step<>(that.getData().getLocatable(), mode.getType()));
            }
          } else {
            // Not visited. Set up node, give it a score, and add it to the system
            Node nextNode = new Node(new Step<>(next.getKey(), mode.getType()),
                current,
                next.getKey().distanceTo(destination),
                current.getScore() + next.getValue());
            upcoming.add(nextNode);
            visited.put(next.getKey(), nextNode);
            IndicatorCommon.<T, D>getSearchEventDispatcher().dispatch(new VisitationSearchEvent<>(session, nextNode.getData()));
          }
        }
      }
    }

    // We've exhausted all possibilities. Fail.
    this.state = ResultState.FAILED;
    this.length = Double.MAX_VALUE;
    IndicatorCommon.<T, D>getSearchEventDispatcher().dispatch(new StopPathSearchEvent<>(session, this));
    IndicatorCommon.<T, D>getPathCache().put(origin, destination, ModeTypeGroup.from(modes), null);
    return new TrialResult<>(Optional.empty(), true);
  }

  public static final record TrialResult<T extends Cell<T, D>, D>(Optional<Path<T, D>> path,
                                                                  boolean changedProblem) {
  }

  class Node {
    @Getter
    private final double proximity;
    @Getter
    @Setter
    private Step<T, D> data;
    @Getter
    @Setter
    private Node previous;
    @Getter
    @Setter
    private double score;

    public Node(@NotNull Step<T, D> data, Node previous, double proximity, double score) {
      this.data = data;
      this.previous = previous;
      this.proximity = proximity;
      this.score = score;
    }

  }

}
