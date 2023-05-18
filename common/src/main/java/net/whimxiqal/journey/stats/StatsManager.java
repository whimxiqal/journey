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

  public void initialize() {
    if (task != null) {
      throw new IllegalStateException("We're already initialized");
    }
    lastStored = System.currentTimeMillis();
    task = Journey.get().proxy().schedulingManager().scheduleRepeat(this::store, false, UPDATE_PERIOD);
  }

  private synchronized void store() {
    long now = System.currentTimeMillis();
    if (now - lastStored < MS_PER_HOUR) {
      return;
    }

    lastStored = now;
    Statistics.SEARCHES_PER_HOUR.store();
    Statistics.BLOCKS_TRAVELLED_PER_HOUR.store();
  }

  public void shutdown() {
    if (task != null) {
      Journey.get().proxy().schedulingManager().cancelTask(task);
      task = null;
    }
  }

}
