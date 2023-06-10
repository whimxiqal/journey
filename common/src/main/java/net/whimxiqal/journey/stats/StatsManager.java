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

  private static final int MS_PER_HOUR = 1000 * 60 * 60;
  private static final int UPDATE_PERIOD = 20 * 60 * 10;  // 10 minutes
  private UUID task;
  private long lastStored = 0;
  private int searches = 0;  // per hour
  private double blocksTravelled = 0; // per hour

  private int storedSearches = 0; // per hour
  private int storedBlocksTravelled = 0;  // per hour

  public void initialize() {
    synchronized (this) {
      if (task != null) {
        throw new IllegalStateException("We're already initialized");
      }
      lastStored = System.currentTimeMillis();
      reset();
      task = Journey.get().proxy().schedulingManager().scheduleRepeat(this::store, false, UPDATE_PERIOD);
    }
  }

  private void store() {
    synchronized (this) {
      long now = System.currentTimeMillis();
      if (now - lastStored < MS_PER_HOUR) {
        return;
      }

      lastStored = now;
      storedSearches = searches;
      storedBlocksTravelled = (int) Math.floor(blocksTravelled);
      reset();
    }
  }

  private void reset() {
    // already synchronized
    searches = 0;
    blocksTravelled = 0;
  }

  /**
   * Increment the number of searches that have occurred. Thread-safe.
   */
  public void incrementSearches() {
    synchronized (this) {
      searches++;
    }
  }

  /**
   * Get the number of searches in the last hour. Thread-safe.
   *
   * @return searches
   */
  public int searches() {
    synchronized (this) {
      return storedSearches;
    }
  }

  /**
   * Increment the number of blocks that have been travelled. Thread-safe.
   */
  public synchronized void addBlocksTravelled(double blocks) {
    synchronized (this) {
      blocksTravelled += blocks;
    }
  }

  /**
   * Get the number of blocks travelled in the last hour. Thread-safe.
   *
   * @return blocks travelled
   */
  public synchronized int blocksTravelled() {
    synchronized (this) {
      return storedBlocksTravelled;
    }
  }

  public void shutdown() {
    Journey.logger().debug("[Stats Manager] Shutting down...");
    if (task != null) {
      Journey.get().proxy().schedulingManager().cancelTask(task);
      task = null;
    }
  }

}
