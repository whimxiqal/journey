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

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.Getter;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.data.DataAccessException;
import net.whimxiqal.journey.navigation.Mode;
import net.whimxiqal.journey.navigation.Path;
import net.whimxiqal.journey.search.function.CostFunction;
import net.whimxiqal.journey.search.function.EuclideanDistanceCostFunction;
import net.whimxiqal.journey.search.function.PlanarOrientedCostFunction;

/**
 * An extension of {@link AbstractPathTrial} where the goal of the trial is to find a path to
 * a specific predefined destination.
 */
public class PathTrial extends AbstractPathTrial {

  public static final double SUFFICIENT_COMPLETION_DISTANCE_SQUARED = 0;
  private static final int MAX_PREPARED_CHUNKS = 10;
  private static boolean loggedMaxCacheHit = false;  // only log this message once
  @Getter
  private final Cell destination;

  private PathTrial(SearchSession session,
                    Cell origin,
                    Cell destination,
                    Collection<Mode> modes,
                    double length,
                    Path path,
                    ResultState state,
                    boolean fromCache,
                    boolean saveOnComplete) {
    super(session, origin, modes,
        costFunction(destination),
        new EuclideanDistanceCostFunction(destination),
        node -> node.getData().location().distanceToSquared(destination)
            <= SUFFICIENT_COMPLETION_DISTANCE_SQUARED,
        length,
        path,
        state,
        fromCache,
        saveOnComplete);
    this.destination = destination;
  }

  private static CostFunction costFunction(Cell destination) {
    return new PlanarOrientedCostFunction(destination);
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
        ResultState.STOPPED_SUCCESSFUL, false, false);
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
        ResultState.STOPPED_FAILED, false, false);
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
                                      Collection<Mode> modes,
                                      boolean saveOnComplete) {
    return new PathTrial(session, origin, destination,
        modes,
        costFunction(destination).apply(origin), null,
        ResultState.IDLE, false, saveOnComplete);
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
        path.getCost(), path,
        ResultState.STOPPED_SUCCESSFUL,
        true, false);
  }

  @Override
  protected void prepareIo() {
    chunkCache.prepareChunks(upcoming.peek().getData().location(), destination, MAX_PREPARED_CHUNKS);
  }

  @Override
  protected void cacheSuccess() {
    Journey.get().proxy().schedulingManager().schedule(() -> {
      if (Journey.get().dataManager().pathRecordManager().totalRecordCellCount() + getLength() > Settings.MAX_CACHED_CELLS.getValue()) {
        if (!loggedMaxCacheHit) {
          Journey.logger().warn("The Journey database has cached the max number of cells allowed in the config file. Raise this number to continue caching results.");
          loggedMaxCacheHit = true;
        }
        return;
      }
      try {
        Journey.get().dataManager().pathRecordManager().report(
            this,
            getModes().stream().map(Mode::type).collect(Collectors.toSet()),
            System.currentTimeMillis() - startExecutionTime);
      } catch (DataAccessException e) {
        Journey.logger().error("SQL error trying to cache a path.");
      }
    }, true);
  }

}