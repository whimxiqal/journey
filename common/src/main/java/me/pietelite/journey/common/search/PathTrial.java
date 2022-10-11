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

import java.util.Collection;
import lombok.Getter;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.Mode;
import me.pietelite.journey.common.navigation.Path;

/**
 * An extension of {@link FlexiblePathTrial} where the goal of the trial is to find a path to
 * a specific predefined destination.
 */
public class PathTrial extends FlexiblePathTrial {

  public static final double SUFFICIENT_COMPLETION_DISTANCE_SQUARED = 0;
  @Getter
  private final Cell destination;

  private PathTrial(SearchSession session,
                    Cell origin,
                    Cell destination,
                    Collection<Mode> modes,
                    double length,
                    Path path,
                    ResultState state,
                    boolean fromCache) {
    super(session, origin, modes,
        scoringFunction(destination),
        node -> node.getData().location().distanceToSquared(destination)
            <= SUFFICIENT_COMPLETION_DISTANCE_SQUARED,
        length,
        path,
        state,
        fromCache);
    this.destination = destination;
  }

  private static ScoringFunction scoringFunction(Cell destination) {
    return new ScoringFunction(node -> -node.getData().location().distanceToSquared(destination),
        ScoringFunction.Type.EUCLIDEAN_DISTANCE);
  }

  /**
   * Get a path trial that is already determined to be successful.
   * Any attempts will result in success.
   *
   * @param session     the session
   * @param origin      the origin of the path
   * @param destination the destination of the path
   * @param modes       the types of modes used
   * @param path        the path
   * @return the successful path trial
   */
  public static PathTrial successful(SearchSession session,
                                     Cell origin, Cell destination,
                                     Collection<Mode> modes,
                                     Path path) {
    return new PathTrial(session, origin, destination,
        modes,
        path.getCost(), path,
        ResultState.STOPPED_SUCCESSFUL, false);
  }

  /**
   * Get a path trial that is already determined to have failed.
   * Any attempts will result in failure.
   *
   * @param session     the session
   * @param origin      the origin of the path
   * @param destination the destination of the path
   * @return the path trial
   */
  public static PathTrial failed(SearchSession session,
                                 Cell origin, Cell destination,
                                 Collection<Mode> modes) {
    return new PathTrial(session, origin, destination,
        modes,
        Double.MAX_VALUE, null,
        ResultState.STOPPED_FAILED, false);
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
   * @return the path trial
   */
  public static PathTrial approximate(SearchSession session,
                                      Cell origin, Cell destination,
                                      Collection<Mode> modes) {
    return new PathTrial(session, origin, destination,
        modes,
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
   * @return the path trial
   */
  public static PathTrial cached(SearchSession session,
                                 Cell origin, Cell destination,
                                 Collection<Mode> modes,
                                 Path path) {
    return new PathTrial(session, origin, destination,
        modes,
        path == null ? origin.distanceTo(destination) : path.getCost(), path,
        path == null ? ResultState.STOPPED_FAILED : ResultState.STOPPED_SUCCESSFUL,
        true);
  }

}