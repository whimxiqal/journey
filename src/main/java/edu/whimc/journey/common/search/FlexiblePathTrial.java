/*
 * MIT License
 *
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
 *
 */

package edu.whimc.journey.common.search;

import edu.whimc.journey.common.JourneyCommon;
import edu.whimc.journey.common.navigation.Cell;
import edu.whimc.journey.common.navigation.Mode;
import edu.whimc.journey.common.navigation.ModeType;
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
import java.util.function.Function;
import java.util.function.Predicate;
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
public class FlexiblePathTrial<T extends Cell<T, D>, D> implements Resulted {

  // TODO bring this max size up to something larger like a million but
  //  create a way to identify whether a long-running operation
  //  is likely to be a failure a different way than maxing out memory.
  //  For example, keep track of the best distance so far and if no
  //  significant improvement has been made in a while, then assume it will never.
  public static final int MAX_SIZE = 10000;

  private final SearchSession<T, D> session;
  @Getter
  private final T origin;
  @Getter
  private final D domain;
  private final Scorer<T, D> scorer;
  private final Completer<T, D> completer;
  @Getter
  private double length;
  @Getter
  private Path<T, D> path;
  @Getter
  private ResultState state;
  @Getter
  private boolean fromCache;

  public FlexiblePathTrial(SearchSession<T, D> session,
                           T origin,
                           Scorer<T, D> scorer,
                           Completer<T, D> completer) {
    this.session = session;
    this.origin = origin;
    this.domain = origin.getDomain();
    this.scorer = scorer;
    this.completer = completer;
  }

  FlexiblePathTrial(SearchSession<T, D> session,
                    T origin,
                    Scorer<T, D> scorer,
                    Completer<T, D> completer,
                    double length,
                    Path<T, D> path,
                    ResultState state,
                    boolean fromCache) {
    this.session = session;
    this.origin = origin;
    this.domain = origin.getDomain();
    this.scorer = scorer;
    this.completer = completer;
    this.length = length;
    this.path = path;
    this.state = state;
    this.fromCache = fromCache;
  }

  private FlexiblePathTrial.TrialResult<T, D> resultFail() {
    this.state = ResultState.STOPPED_FAILED;
    this.length = Double.MAX_VALUE;
    this.fromCache = false;
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopPathSearchEvent<>(session, this));
    return new TrialResult<>(Optional.empty(), true);
  }

  private FlexiblePathTrial.TrialResult<T, D> resultSucceed(double length,
                                                            List<Step<T, D>> steps) {
    this.state = ResultState.STOPPED_SUCCESSFUL;
    this.length = length;
    this.path = new Path<>(origin, new ArrayList<>(steps), length);
    this.fromCache = false;
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopPathSearchEvent<>(session, this));
    return new TrialResult<>(Optional.of(this.path), true);
  }

  private FlexiblePathTrial.TrialResult<T, D> resultCancel() {
    this.state = ResultState.STOPPED_CANCELED;
    this.length = Double.MAX_VALUE;
    this.fromCache = false;
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopPathSearchEvent<>(session, this));
    return new TrialResult<>(Optional.empty(), true);
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

    // Return the saved states, but only if we want that result.
    //  If we don't want to use the cache, but this result is from the cache,
    //  then don't return this.
    if (!this.fromCache || useCacheIfPossible) {
      if (this.state == ResultState.STOPPED_SUCCESSFUL) {
        if (path.test(modes)) {
          return new TrialResult<>(Optional.of(path), false);
        }
      } else if (this.state == ResultState.STOPPED_FAILED) {
        return new TrialResult<>(Optional.empty(), false);
      }
    }

    // Dispatch a starting event
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StartPathSearchEvent<>(session, this));
    if (this instanceof PathTrial<T, D> pathTrial) {
      System.out.println("Searching for new path from " + origin + " to " + pathTrial.getDestination());
    }

    Queue<Node<T, D>> upcoming = new PriorityQueue<>(Comparator.comparingDouble(node -> -scorer.apply(node)));
    Map<T, Node<T, D>> visited = new HashMap<>();

    Node<T, D> originNode = new Node<>(new Step<>(origin, ModeType.NONE),
        null, 0);
    upcoming.add(originNode);
    visited.put(origin, originNode);
    JourneyCommon.<T, D>getSearchEventDispatcher()
        .dispatch(new VisitationSearchEvent<>(session, originNode.getData()));

    Node<T, D> current;
    while (!upcoming.isEmpty()) {
      if (session.state.isCancelFailed()) {
        // Canceled! Fail here, but don't cache it because it's not the true solution for this path.
        return resultCancel();
      }

      if (visited.size() > MAX_SIZE) {
        // We ran out of allocated memory. Let's just call it here and say we failed and cache the failure.
        return resultFail();
      }

      current = upcoming.poll();
      assert current != null;
      JourneyCommon.<T, D>getSearchEventDispatcher()
          .dispatch(new StepSearchEvent<>(session, originNode.getData()));

      if (completer.test(current)) {
        // We found it!
        double length = current.getScore();
        LinkedList<Step<T, D>> steps = new LinkedList<>();
        do {
          steps.addFirst(current.getData());
          current = current.getPrevious();
        } while (current != null);
        return resultSucceed(length, steps);
      }

      // Need to keep going
      for (Mode<T, D> mode : modes) {
        for (Mode<T, D>.Option option : mode.getDestinations(current.getData().location())) {
          if (visited.containsKey(option.getLocation())) {
            // Already visited, but see if it is better to come from this new direction
            Node<T, D> that = visited.get(option.getLocation());
            if (current.getScore() + option.getDistance() < that.getScore()) {
              that.setPrevious(current);
              that.setScore(current.getScore() + option.getDistance());
              that.setData(new Step<>(that.getData().location(), mode.getType()));
            }
          } else {
            // Not visited. Set up node, give it a score, and add it to the system
            Node<T, D> nextNode = new Node<>(new Step<>(option.getLocation(), mode.getType()),
                current,
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
    return resultFail();
  }

  /**
   * An interface to represent the score of a given node.
   * Of all the nodes that are currently in the running for the
   * "next best node to try" throughout this algorithm,
   * the one with the highest score is chosen next.
   *
   * @param <T> the location type
   * @param <D> the domain type
   */
  @FunctionalInterface
  public interface Scorer<T extends Cell<T, D>, D> extends Function<Node<T, D>, Double> {
  }

  /**
   * An interface to represent when a node is considered successful and therefore
   * the end of a successful path.
   * At this point in the algorithm, the path up until and through this node is returned.
   *
   * @param <T> the location type
   * @param <D> the domain type
   */
  @FunctionalInterface
  public interface Completer<T extends Cell<T, D>, D> extends Predicate<Node<T, D>> {
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

  static class Node<T extends Cell<T, D>, D> {
    @Getter
    @Setter
    private Step<T, D> data;
    @Getter
    @Setter
    private Node<T, D> previous;
    @Getter
    @Setter
    private double score;

    public Node(@NotNull Step<T, D> data, Node<T, D> previous, double score) {
      this.data = data;
      this.previous = previous;
      this.score = score;
    }

  }

}