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

package net.whimxiqal.journey.stats;

import java.util.UUID;
import net.whimxiqal.journey.Journey;

public class StatsManager {

  private static final int TICK_PERIOD = 20 * 60 * 60;  // one hour
  private UUID task;
  private int searches = 0;
  private double blocksTravelled = 0;

  private int storedSearches = 0;
  private int storedBlocksTravelled = 0;

  public void initialize() {
    if (task != null) {
      throw new IllegalStateException("We're already initialized");
    }
    reset();
    task = Journey.get().proxy().schedulingManager().scheduleRepeat(() -> {
        store();
        reset();
    }, false, TICK_PERIOD);
  }

  private synchronized void store() {
    storedSearches = searches;
    storedBlocksTravelled = (int) Math.floor(blocksTravelled);
  }

  private synchronized void reset() {
    searches = 0;
    blocksTravelled = 0;
  }

  /**
   * Increment the number of searches that have occurred. Thread-safe.
   */
  public synchronized void incrementSearches() {
    searches++;
  }

  /**
   * Get the number of searches in the last hour. Thread-safe.
   * @return searches
   */
  public synchronized int searches() {
    return storedSearches;
  }

  /**
   * Increment the number of blocks that have been travelled. Thread-safe.
   */
  public synchronized void addBlocksTravelled(double blocks) {
    blocksTravelled += blocks;
  }

  /**
   * Get the number of blocks travelled in the last hour. Thread-safe.
   * @return blocks travelled
   */
  public synchronized int blocksTravelled() {
    return storedBlocksTravelled;
  }

  public void shutdown() {
    if (task != null) {
      Journey.get().proxy().schedulingManager().cancelTask(task);
      task = null;
    }
  }

}
