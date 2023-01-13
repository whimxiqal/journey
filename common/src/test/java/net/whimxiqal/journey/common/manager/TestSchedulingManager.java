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

package net.whimxiqal.journey.common.manager;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class TestSchedulingManager implements SchedulingManager {

  public static final int TICK_PERIOD_MS = 10;

  private static class Task {
    Runnable runnable;
    long executionTime;
  }

  private final PriorityQueue<Task> queue;
  private final ReentrantLock queueLock = new ReentrantLock();

  public TestSchedulingManager() {
    queue = new PriorityQueue<>(Comparator.comparing(task -> task.executionTime));
    startMainThread();
  }

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
    long delayMs = tickDelay / 20 * 1000L;
    if (async) {
      Thread thread = new Thread(() -> {
        try {
          Thread.sleep(delayMs);
          runnable.run();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      });
      thread.start();
    } else {
      Task task = new Task();
      task.runnable = runnable;
      task.executionTime = System.currentTimeMillis() + delayMs;
      try {
        queueLock.lock();
        queue.add(task);
      } finally {
        queueLock.unlock();
      }
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

  public void startMainThread() {
    Thread thread = new Thread(() -> {
      long currentTime = System.currentTimeMillis();
      try {
        queueLock.lock();
        while (!queue.isEmpty() && queue.peek().executionTime < currentTime) {
          queue.remove().runnable.run();
        }
      } finally {
        queueLock.unlock();
      }
      try {
        Thread.sleep(TICK_PERIOD_MS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }, "TestSchedulingManagerThread");
    thread.start();
  }
}
