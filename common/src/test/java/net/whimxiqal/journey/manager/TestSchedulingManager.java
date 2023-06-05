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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import net.whimxiqal.journey.Journey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestSchedulingManager implements SchedulingManager {

  public static final long MS_PER_TICK = 1000L / 20L;
  private final ScheduledExecutorService mainThread;
  private final ScheduledExecutorService asyncThreads;
  private final Map<UUID, ScheduledFuture<?>> futureMap;
  private final AtomicLong mainThreadId = new AtomicLong(-1);

  public TestSchedulingManager() {
    mainThread = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread thread = new Thread(r);
      thread.setName("Main Server Thread");
      return thread;
    });
    asyncThreads = Executors.newScheduledThreadPool(4, r -> {
      Thread thread = new Thread(r);
      thread.setName("Async Server Thread " + thread.getId());
      return thread;
    });
    futureMap = new ConcurrentHashMap<>();
  }

  private static Runnable runWithCatch(Runnable runnable) {
    return () -> {
      try {
        runnable.run();
      } catch (Exception e) {
        e.printStackTrace();
      }
    };
  }

  public static <T> T runOnMainThread(Supplier<T> supplier) {
    CompletableFuture<T> future = new CompletableFuture<>();
    AtomicBoolean error = new AtomicBoolean(false);
    Journey.get().proxy().schedulingManager().schedule(() -> {
      T result = null;
      try {
        result = supplier.get();
      } catch (Throwable e) {
        e.printStackTrace();
        error.set(true);
      } finally {
        future.complete(result);
      }
    }, false);
    T result = null;
    try {
      result = future.get();
    } catch (InterruptedException | ExecutionException e) {
      Assertions.fail(e);
    }
    Assertions.assertFalse(error.get(), "There was an error on the main thread");
    return result;
  }

  public static void runOnMainThread(Runnable runnable) {
    runOnMainThread(() -> {
      runnable.run();
      return null;
    });
  }

  @Override
  public void schedule(Runnable runnable, boolean async) {
    if (async) {
      asyncThreads.execute(runWithCatch(runnable));
    } else {
      mainThread.execute(runWithCatch(runnable));
    }
  }

  @Override
  public void schedule(Runnable runnable, boolean async, int tickDelay) {
    ScheduledExecutorService service = async ? asyncThreads : mainThread;
    service.schedule(runnable, tickDelay * MS_PER_TICK, TimeUnit.MILLISECONDS);
  }

  @Override
  public UUID scheduleRepeat(Runnable runnable, boolean async, int tickPeriod) {
    UUID taskUuid = UUID.randomUUID();
    ScheduledExecutorService service = async ? asyncThreads : mainThread;
    long periodMs = tickPeriod * MS_PER_TICK;
    ScheduledFuture<?> future = service.scheduleAtFixedRate(runWithCatch(runnable), periodMs, periodMs, TimeUnit.MILLISECONDS);
    futureMap.put(taskUuid, future);
    return taskUuid;
  }

  @Override
  public void cancelTask(UUID taskId) {
    futureMap.get(taskId).cancel(true);
  }

  @Override
  public boolean isMainThread() {
    return Thread.currentThread().getId() == mainThreadId.get();
  }

  @Override
  public void initialize() {
    if (mainThreadId.get() != -1) {
      // already initialized
      return;
    }
    CompletableFuture<Void> future = new CompletableFuture<>();
    mainThread.execute(() -> {
      mainThreadId.set(Thread.currentThread().getId());
      future.complete(null);
    });
    try {
      future.get();  // wait for it to be done
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void shutdown() {
    mainThread.shutdown();
    asyncThreads.shutdown();
  }

  @Test
  void sanityCheck() throws ExecutionException, InterruptedException {
    TestSchedulingManager schedulingManager = new TestSchedulingManager();
    schedulingManager.initialize();

    // test isMainThread
    CompletableFuture<Boolean> future1 = new CompletableFuture<>();
    schedulingManager.schedule(() -> future1.complete(schedulingManager.isMainThread()), false);
    Assertions.assertTrue(future1.get());

    CompletableFuture<Boolean> future2 = new CompletableFuture<>();
    schedulingManager.schedule(() -> future2.complete(schedulingManager.isMainThread()), true);
    Assertions.assertFalse(future2.get());

    // test repeatable
    AtomicInteger counter1 = new AtomicInteger(0);
    AtomicInteger counter2 = new AtomicInteger(0);
    AtomicReference<UUID> taskUuid1 = new AtomicReference<>();
    AtomicReference<UUID> taskUuid2 = new AtomicReference<>();
    CompletableFuture<Void> future3 = new CompletableFuture<>();
    CompletableFuture<Void> future4 = new CompletableFuture<>();
    taskUuid1.set(schedulingManager.scheduleRepeat(() -> {
      if (counter1.get() == 10) {
        return;
      }
      int current = counter1.incrementAndGet();
      if (current == 10) {
        future3.complete(null);
      }
    }, false, 1));
    taskUuid2.set(schedulingManager.scheduleRepeat(() -> {
      if (counter2.get() == 10) {
        return;
      }
      int current = counter2.incrementAndGet();
      if (current == 10) {
        future4.complete(null);
      }
    }, false, 1));
    future3.get();
    future4.get();
    Assertions.assertEquals(10, counter1.get());
    Assertions.assertEquals(10, counter2.get());
    schedulingManager.cancelTask(taskUuid1.get());
    schedulingManager.cancelTask(taskUuid2.get());

    // test exceptions don't stop new scheduled tasks
    schedulingManager.schedule(() -> {
      throw new RuntimeException();
    }, true);
    CompletableFuture<Void> future5 = new CompletableFuture<>();
    AtomicBoolean toggle = new AtomicBoolean(false);
    schedulingManager.schedule(() -> {
      toggle.set(true);
      future5.complete(null);
    }, true);
    future5.get();
    Assertions.assertTrue(toggle.get());

    schedulingManager.shutdown();
  }

}
