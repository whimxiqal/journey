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
import edu.whimc.indicator.common.navigation.Path;
import edu.whimc.indicator.common.navigation.Step;
import edu.whimc.indicator.common.search.event.SearchDispatcher;
import edu.whimc.indicator.common.search.event.StepSearchEvent;
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
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class PathTrial<T extends Cell<T, D>, D> {

  public static final int MAX_SIZE = 10000;
  public static final double SUFFICIENT_COMPLETION_DISTANCE_SQUARED = 2;

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
  private State state;

  public static <T extends Cell<T, D>, D> PathTrial<T, D> successful(SearchSession<T, D> session,
                                                                     T origin, T destination,
                                                                     Path<T, D> path) {
    return new PathTrial<>(session, origin, destination, path, State.SUCCESSFUL);
  }

  public static <T extends Cell<T, D>, D> PathTrial<T, D> failed(SearchSession<T, D> session,
                                                                 T origin, T destination) {
    return new PathTrial<>(session, origin, destination, null, State.FAILED);
  }

  public static <T extends Cell<T, D>, D> PathTrial<T, D> approximate(SearchSession<T, D> session,
                                            T origin, T destination) {
    return new PathTrial<>(session, origin, destination, null, State.UNKNOWN);
  }

  public static <T extends Cell<T, D>, D> PathTrial<T, D> cached(SearchSession<T, D> session,
                                                                      T origin, T destination,
                                                                      Path<T, D> path) {
    return new PathTrial<>(session, origin, destination, null, State.CACHED);
  }

  private PathTrial(SearchSession<T, D> session,
                   T origin, T destination,
                   Path<T, D> path,
                   State state) {
    if (!origin.getDomain().equals(destination.getDomain())) {
      throw new IllegalArgumentException("The domain of the origin and destination must be the same");
    }
    this.session = session;
    this.origin = origin;
    this.destination = destination;
    this.domain = origin.getDomain();
    this.path = path;
    this.length = origin.distanceTo(destination);
  }

  private void fail() {
    this.length = Double.MAX_VALUE;
    this.state = State.FAILED;
  }

  @NotNull
  public Optional<Path<T, D>> attempt(SearchSession<T, D> session, Collection<Mode<T, D>> modes, boolean useCache) {
    if (!origin.getDomain().equals(destination.getDomain())) {
      throw new IllegalArgumentException("The input locatables ["
          + origin + " and "
          + destination
          + "] must have the same domain to search for a path");
    }

    if (this.state == State.SUCCESSFUL) {
      return Optional.of(path);
    }

    if (this.state == State.FAILED) {
      return Optional.empty();
    }

    if (useCache && this.state == State.CACHED) {
      return Optional.ofNullable(path);
    }

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
      if (session.state.isCanceled() || visited.size() > MAX_SIZE) {
        this.state = State.FAILED;
        return Optional.empty();
      }

      current = upcoming.poll();
      assert current != null;
      IndicatorCommon.<T, D>getSearchEventDispatcher().dispatch(new StepSearchEvent<>(session, originNode.getData()));

      // We found it!
      if (current.getData().getLocatable().distanceToSquared(destination) <= SUFFICIENT_COMPLETION_DISTANCE_SQUARED) {
        double length = current.getScore();
        LinkedList<Step<T, D>> steps = new LinkedList<>();
        do {
          steps.addFirst(current.getData());
          current = current.getPrevious();
        } while (current != null);
        this.state = State.SUCCESSFUL;
        this.path = new Path<>(origin, new ArrayList<>(steps), length);
        this.length = this.path.getLength();
        return Optional.of(this.path);
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
    this.state = State.FAILED;
    return Optional.empty();
  }

  class Node {
    @Getter
    @Setter
    private Step<T, D> data;
    @Getter
    private final double proximity;
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

  public enum State {
    SUCCESSFUL,
    CACHED,
    FAILED,
    UNKNOWN
  }
}
