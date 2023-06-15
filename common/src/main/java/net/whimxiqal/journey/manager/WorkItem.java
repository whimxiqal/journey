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

package net.whimxiqal.journey.manager;

import java.util.UUID;

/**
 * A unit of work, as seen by the {@link DistributedWorkManager}.
 */
public interface WorkItem {

  /**
   * The id of the owner of this work, for load balancing.
   */
  UUID owner();

  /**
   * Execute work. This, along with {@link #reset()}, will only be run by a single thread at a time, but
   * any execution may be run by a different thread than the last.
   *
   * @return true if done, false if there is more work to do
   */
  boolean run();

  /**
   * Reset all state on this object, and clear any cached data.
   * Run may be re-run on this object, and it would be as if it was called for the first time.
   * This, along with {@link #run()}, will only be run by a single thread at a time, but
   * any execution may be run by a different thread than the last.
   */
  void reset();

}
