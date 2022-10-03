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

package me.pietelite.journey.common.search.event;

import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.Path;
import me.pietelite.journey.common.search.FlexiblePathTrial;
import me.pietelite.journey.common.search.PathTrial;
import me.pietelite.journey.common.search.SearchSession;
import java.util.Collection;

/**
 * An event dispatched when a {@link PathTrial} stops searching
 * for a {@link Path}.
 *
 * @param <T> the location type
 * @param <D> the domain type
 * @see SearchDispatcher
 * @see SearchSession
 */
public class StopPathSearchEvent extends SearchEvent {

  private final FlexiblePathTrial pathTrial;
  private final long executionTime;

  /**
   * General constructor.
   *
   * @param session   the session
   * @param pathTrial the path trial causing this event
   */
  public StopPathSearchEvent(SearchSession session,
                             FlexiblePathTrial pathTrial,
                             long executionTime) {
    super(session);
    this.pathTrial = pathTrial;
    this.executionTime = executionTime;
  }

  /**
   * The path trial being run when this event was dispatched.
   *
   * @return the path trial
   */
  public FlexiblePathTrial getPathTrial() {
    return pathTrial;
  }

  /**
   * Get the total time it took for the process to finish executing, in milliseconds.
   *
   * @return the execution time
   */
  public long getExecutionTime() {
    return executionTime;
  }

  @Override
  EventType type() {
    return EventType.STOP_PATH;
  }
}
