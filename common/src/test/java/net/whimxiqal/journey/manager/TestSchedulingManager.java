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

import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class TestSchedulingManager implements SchedulingManager {

  public static final int TICK_PERIOD_MS = 10;
  private final PriorityBlockingQueue<Task> queue;
  private final ExecutorService asyncService;
  private final Set<UUID> canceledTasks;

  public TestSchedulingManager() {
    queue = new PriorityBlockingQueue<>(10, Comparator.comparing(task -> task.executionTime));
    asyncService = Executors.newFixedThreadPool(2);
    canceledTasks = ConcurrentHashMap.newKeySet();
  }

  @Override
  public void schedule(Runnable runnable, boolean async) {
    if (async) {
      asyncService.execute(runnable);
    } else {
      runnable.run();
    }
  }

  @Override
  public void schedule(Runnable runnable, boolean async, int tickDelay) {
    long delayMs = tickDelay / 20 * 1000L;
    if (async) {
      asyncService.execute(() -> {
        try {
          Thread.sleep(delayMs);
          runnable.run();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      });
    } else {
      Task task = new Task();
      task.runnable = runnable;
      task.executionTime = System.currentTimeMillis() + delayMs;
      queue.add(task);
    }
  }

  @Override
  public UUID scheduleRepeat(Runnable runnable, boolean async, int tickPeriod) {
    UUID taskUuid = UUID.randomUUID();
    long periodMs = tickPeriod / 20 * 1000L;
    if (async) {
      Thread thread = new Thread(() -> {
        while (true) {
          if (canceledTasks.contains(taskUuid)) {
            canceledTasks.remove(taskUuid);
            break;
          }
          try {
            Thread.sleep(periodMs);
            runnable.run();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });
      thread.start();
    } else {
      AtomicReference<Runnable> repeatable = new AtomicReference<>();
      repeatable.set(() -> {
        if (canceledTasks.contains(taskUuid)) {
          canceledTasks.remove(taskUuid);
          return;
        }
        runnable.run();
        Task task = new Task();
        task.runnable = repeatable.get();
        task.executionTime = System.currentTimeMillis() + periodMs;
        queue.add(task);
      });
      repeatable.get().run();
    }
    return taskUuid;
  }

  @Override
  public void cancelTask(UUID taskId) {
    canceledTasks.add(taskId);
  }

  public void startMainThread() {
    Thread thread = new Thread(() -> {
      while (true) {
        long currentTime = System.currentTimeMillis();
        while (!queue.isEmpty() && queue.peek().executionTime < currentTime) {
          queue.remove().runnable.run();
        }
        try {
          Thread.sleep(TICK_PERIOD_MS);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }, "TestSchedulingManagerThread");
    thread.start();
  }

  private static class Task {
    Runnable runnable;
    long executionTime;
  }
}
