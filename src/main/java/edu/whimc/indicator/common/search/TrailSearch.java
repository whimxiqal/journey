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

import edu.whimc.indicator.common.path.*;
import edu.whimc.indicator.common.search.tracker.SearchTracker;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TrailSearch<T extends Cell<T, D>, D> {

  public static final int MAX_SIZE = 10000;
  public static final double SUFFICIENT_COMPLETION_DISTANCE_SQUARED = 2;

  @Setter
  private Consumer<Step<T, D>> visitationCallback = loc -> {
  };

  @Setter
  private Consumer<Step<T, D>> stepCallback = loc -> {
  };


  @Nullable
  public Trail<T, D> findOptimalTrail(TrailSearchRequest<T, D> request, Collection<Mode<T, D>> modes, SearchTracker<T, D> tracker) {
    return findOptimalTrail(request.getOrigin(),
        request.getDestination(),
        modes,
        request.getCancellation(),
        tracker);
  }

  @Nullable
  public Trail<T, D> findOptimalTrail(T origin, T destination,
                                      Collection<Mode<T, D>> modes,
                                      Supplier<Boolean> cancellation,
                                      SearchTracker<T, D> tracker) {
    if (!origin.getDomain().equals(destination.getDomain())) {
      throw new IllegalArgumentException("The input locatables ["
          + origin + " and "
          + destination
          + "] must have the same domain to search for a trail");
    }
    Queue<Node> upcoming = new PriorityQueue<>(Comparator.comparingDouble(Node::getProximity));
    Map<T, Node> visited = new HashMap<>();

    Node originNode = new Node(new Step<>(origin, ModeType.NONE),
        null,
        origin.distanceTo(destination), 0);
    upcoming.add(originNode);
    visited.put(origin, originNode);
    visitationCallback.accept(originNode.getData());

    Node current;
    while (!upcoming.isEmpty()) {
      if (cancellation.get()) {
        return null;  // Cancelled
      }
      if (visited.size() > MAX_SIZE) {
        return Trail.INVALID();  // Too large, couldn't find a solution
      }

      current = upcoming.poll();
      assert current != null;
      stepCallback.accept(current.getData());

      // We found it!
      if (current.getData().getLocatable().distanceToSquared(destination) <= SUFFICIENT_COMPLETION_DISTANCE_SQUARED) {
        double length = current.getScore();
        LinkedList<Step<T, D>> steps = new LinkedList<>();
        do {
          steps.addFirst(current.getData());
          current = current.getPrevious();
        } while (current != null);
        return new Trail<>(new ArrayList<>(steps), length);
      }

      // Need to keep going
      for (Mode<T, D> mode : modes) {
        for (Map.Entry<T, Double> next : mode.getDestinations(current.getData().getLocatable(), tracker).entrySet()) {
          if (visited.containsKey(next.getKey())) {
            // Already visited, but see if it is better to come from this new direction
            if (current.getScore() + next.getValue() < visited.get(next.getKey()).getScore()) {
              visited.get(next.getKey()).setPrevious(current);
              visited.get(next.getKey()).setScore(current.getScore() + next.getValue());
              visited.get(next.getKey()).getData().setModeType(mode.getType());
            }
          } else {
            // Not visited. Set up node, give it a score, and add it to the system
            Node nextNode = new Node(new Step<>(next.getKey(), mode.getType()),
                current,
                next.getKey().distanceTo(destination),
                current.getScore() + next.getValue());
            upcoming.add(nextNode);
            visited.put(next.getKey(), nextNode);
            visitationCallback.accept(nextNode.getData());
          }
        }
      }
    }
    return Trail.INVALID();  // Nothing found
  }

  class Node {
    @Getter
    private final Step<T, D> data;
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

}
