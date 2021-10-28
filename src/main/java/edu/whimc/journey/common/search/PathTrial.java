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

package edu.whimc.journey.common.search;

import edu.whimc.journey.common.JourneyCommon;
import edu.whimc.journey.common.navigation.Cell;
import edu.whimc.journey.common.navigation.Mode;
import edu.whimc.journey.common.navigation.ModeType;
import edu.whimc.journey.common.navigation.ModeTypeGroup;
import edu.whimc.journey.common.navigation.Path;
import edu.whimc.journey.common.navigation.Step;
import edu.whimc.journey.common.search.event.StartPathSearchEvent;
import edu.whimc.journey.common.search.event.StepSearchEvent;
import edu.whimc.journey.common.search.event.StopPathSearchEvent;
import edu.whimc.journey.common.search.event.VisitationSearchEvent;
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
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * An attempt to calculate a {@link Path} encapsulated into an object.
 *
 * @param <T> the location type
 * @param <D> the domain type
 * @see Path
 * @see SearchSession
 * @see ItineraryTrial
 */
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
  private boolean fromCache;

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

  /**
   * Get a path trial that is already determined to be successful.
   * Any attempts will result in success.
   *
   * @param session     the session
   * @param origin      the origin of the path
   * @param destination the destination of the path
   * @param path        the path
   * @param <T>         the location type
   * @param <D>         the domain type
   * @return the successful path trial
   */
  public static <T extends Cell<T, D>, D> PathTrial<T, D> successful(SearchSession<T, D> session,
                                                                     T origin, T destination,
                                                                     Path<T, D> path) {
    return new PathTrial<>(session, origin, destination,
        path.getLength(), path,
        ResultState.SUCCESSFUL, false);
  }

  /**
   * Get a path trial that is already determined to have failed.
   * Any attempts will result in failure.
   *
   * @param session     the session
   * @param origin      the origin of the path
   * @param destination the destination of the path
   * @param <T>         the location type
   * @param <D>         the domain type
   * @return the path trial
   */
  public static <T extends Cell<T, D>, D> PathTrial<T, D> failed(SearchSession<T, D> session,
                                                                 T origin, T destination) {
    return new PathTrial<>(session, origin, destination,
        Double.MAX_VALUE, null,
        ResultState.FAILED, false);
  }

  /**
   * Get a path trial that has not yet been calculated.
   * We will approximate the result as the cartesian distance between the
   * origin and destination,
   * but a more precise answer will be calculated the first time it is attempted.
   *
   * @param session     the session
   * @param origin      the origin
   * @param destination the destination
   * @param <T>         the location type
   * @param <D>         the domain type
   * @return the path trial
   */
  public static <T extends Cell<T, D>, D> PathTrial<T, D> approximate(SearchSession<T, D> session,
                                                                      T origin, T destination) {
    return new PathTrial<>(session, origin, destination,
        origin.distanceTo(destination), null,
        ResultState.IDLE, false);
  }

  /**
   * Get a path trial that has some result determined from the cache.
   *
   * @param session     the session
   * @param origin      the origin
   * @param destination the destination
   * @param path        the path
   * @param <T>         the location type
   * @param <D>         the domain type
   * @return the path trial
   */
  public static <T extends Cell<T, D>, D> PathTrial<T, D> cached(SearchSession<T, D> session,
                                                                 T origin, T destination,
                                                                 Path<T, D> path) {
    return new PathTrial<>(session, origin, destination,
        path == null ? origin.distanceTo(destination) : path.getLength(), path,
        path == null ? ResultState.FAILED : ResultState.SUCCESSFUL,
        true);
  }

  private PathTrial.TrialResult<T, D> fail(Collection<Mode<T, D>> modes, boolean cacheIt) {
    this.state = ResultState.FAILED;
    this.length = Double.MAX_VALUE;
    this.fromCache = false;
    if (cacheIt) {
      JourneyCommon.<T, D>getPathCache().put(origin, destination, ModeTypeGroup.from(modes), null);
    }
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopPathSearchEvent<>(session, this));
    return new TrialResult<>(Optional.empty(), true);
  }

  private PathTrial.TrialResult<T, D> succeed(double length,
                                              List<Step<T, D>> steps,
                                              Collection<Mode<T, D>> modes) {
    this.state = ResultState.SUCCESSFUL;
    this.length = length;
    this.path = new Path<>(origin, new ArrayList<>(steps), length);
    this.fromCache = false;
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopPathSearchEvent<>(session, this));
    JourneyCommon.<T, D>getPathCache().put(origin, destination, ModeTypeGroup.from(modes), this.path);
    return new TrialResult<>(Optional.of(this.path), true);
  }

  /**
   * Attempt to calculate a path given some modes of transportation.
   *
   * @param modes              the modes allowed for the caller
   * @param useCacheIfPossible whether the cache should be used for retrieving previous results
   * @return a result object
   */
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
    if (!this.fromCache || useCacheIfPossible) {
      if (this.state == ResultState.SUCCESSFUL) {
        if (path.test(modes)) {
          // The path was cached, it was successful when cached, and we verified it to still be valid.
          return new TrialResult<>(Optional.of(path), false);
        }
        // It was cached as successful but it no longer works... calculate again!
      } else if (this.state == ResultState.FAILED) {
        return new TrialResult<>(Optional.empty(), false);
      }
    }

    // Dispatch a starting event
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StartPathSearchEvent<>(session, this));

    Queue<Node> upcoming = new PriorityQueue<>(Comparator.comparingDouble(Node::getProximity));
    Map<T, Node> visited = new HashMap<>();

    Node originNode = new Node(new Step<>(origin, ModeType.NONE),
        null,
        origin.distanceTo(destination), 0);
    upcoming.add(originNode);
    visited.put(origin, originNode);
    JourneyCommon.<T, D>getSearchEventDispatcher()
        .dispatch(new VisitationSearchEvent<>(session, originNode.getData()));

    Node current;
    while (!upcoming.isEmpty()) {
      if (session.state.isCanceled()) {
        // Canceled! Fail here, but don't cache it because it's not the true solution for this path.
        return fail(modes, false);
      }

      if (visited.size() > MAX_SIZE) {
        // We ran out of allocated memory. Let's just call it here and say we failed and cache the failure.
        return fail(modes, true);
      }

      current = upcoming.poll();
      assert current != null;
      JourneyCommon.<T, D>getSearchEventDispatcher()
          .dispatch(new StepSearchEvent<>(session, originNode.getData()));

      if (current.getData().location().distanceToSquared(destination)
          <= SUFFICIENT_COMPLETION_DISTANCE_SQUARED) {
        // We found it!
        double length = current.getScore();
        LinkedList<Step<T, D>> steps = new LinkedList<>();
        do {
          steps.addFirst(current.getData());
          current = current.getPrevious();
        } while (current != null);
        return succeed(length, steps, modes);
      }

      // Need to keep going
      for (Mode<T, D> mode : modes) {
        for (Mode<T, D>.Option option : mode.getDestinations(current.getData().location())) {
          if (visited.containsKey(option.getLocation())) {
            // Already visited, but see if it is better to come from this new direction
            Node that = visited.get(option.getLocation());
            if (current.getScore() + option.getDistance() < that.getScore()) {
              that.setPrevious(current);
              that.setScore(current.getScore() + option.getDistance());
              that.setData(new Step<>(that.getData().location(), mode.getType()));
            }
          } else {
            // Not visited. Set up node, give it a score, and add it to the system
            Node nextNode = new Node(new Step<>(option.getLocation(), mode.getType()),
                current,
                option.getLocation().distanceTo(destination),
                current.getScore() + option.getDistance());
            upcoming.add(nextNode);
            visited.put(option.getLocation(), nextNode);
            JourneyCommon.<T, D>getSearchEventDispatcher()
                .dispatch(new VisitationSearchEvent<>(session, nextNode.getData()));
          }
        }
      }
    }

    // We've exhausted all possibilities. Fail.
    return fail(modes, true);
  }

  /**
   * A result object to return the result of the {@link #attempt} method.
   *
   * @param <T> the location type
   * @param <D> the domain type
   */
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
