/*
 * MIT License
 *
 * Copyright 2022 Pieter Svenson
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

package me.pietelite.journey.common.manager;

import java.util.UUID;

public class TestSchedulingManager implements SchedulingManager {
  @Override
  public void schedule(Runnable runnable, boolean async) {
    if (async) {
      Thread thread = new Thread(runnable);
      thread.start();
    } else {
      runnable.run();
    }
  }

  @Override
  public void schedule(Runnable runnable, boolean async, int tickDelay) {
    Runnable func = () -> {
      try {
        Thread.sleep(tickDelay / 20 * 1000L);
        runnable.run();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    };
    if (async) {
      Thread thread = new Thread(func);
      thread.start();
    } else {
      func.run();
    }
  }

  @Override
  public UUID scheduleRepeat(Runnable runnable, boolean async, int tickPeriod) {
    // not supported
    return null;
  }

  @Override
  public void cancelTask(UUID taskId) {
    // not supported
  }
}
