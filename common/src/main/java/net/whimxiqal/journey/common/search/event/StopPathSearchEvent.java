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

package net.whimxiqal.journey.common.search.event;

import net.whimxiqal.journey.common.navigation.Path;
import net.whimxiqal.journey.common.search.FlexiblePathTrial;
import net.whimxiqal.journey.common.search.PathTrial;
import net.whimxiqal.journey.common.search.SearchSession;

/**
 * An event dispatched when a {@link PathTrial} stops searching
 * for a {@link Path}.
 *
 * @see SearchDispatcher
 * @see SearchSession
 */
public class StopPathSearchEvent extends SearchEvent {

  private final FlexiblePathTrial pathTrial;
  private final long executionTime;
  private final boolean save;

  /**
   * General constructor.
   *
   * @param session   the session
   * @param pathTrial the path trial causing this event
   */
  public StopPathSearchEvent(SearchSession session,
                             FlexiblePathTrial pathTrial,
                             long executionTime,
                             boolean save) {
    super(session);
    this.pathTrial = pathTrial;
    this.executionTime = executionTime;
    this.save = save;
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

  public boolean shouldSave() {
    return save;
  }

  @Override
  EventType type() {
    return EventType.STOP_PATH;
  }
}
