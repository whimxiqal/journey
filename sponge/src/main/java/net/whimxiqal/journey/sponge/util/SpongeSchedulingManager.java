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

package net.whimxiqal.journey.sponge.util;

import java.util.UUID;
import net.whimxiqal.journey.manager.SchedulingManager;
import net.whimxiqal.journey.sponge.JourneySponge;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;

public class SpongeSchedulingManager implements SchedulingManager {

  @Override
  public void schedule(Runnable runnable, boolean async) {
    Task task = Task.builder().plugin(JourneySponge.get().container()).execute(runnable).build();
    if (async) {
      Sponge.asyncScheduler().submit(task);
    } else {
      Sponge.server().scheduler().submit(task);
    }
  }

  @Override
  public void schedule(Runnable runnable, boolean async, int tickDelay) {
    if (tickDelay <= 0) {
      schedule(runnable, async);
    }
    Task task = Task.builder()
        .plugin(JourneySponge.get().container())
        .execute(runnable)
        .delay(Ticks.of(tickDelay))
        .build();
    if (async) {
      Sponge.asyncScheduler().submit(task);
    } else {
      Sponge.server().scheduler().submit(task);
    }
  }

  @Override
  public UUID scheduleRepeat(Runnable runnable, boolean async, int tickPeriod) {
    int id;
    Task task = Task.builder()
        .plugin(JourneySponge.get().container())
        .execute(runnable)
        .interval(Ticks.of(tickPeriod))
        .build();
    if (async) {
      return Sponge.asyncScheduler().submit(task).uniqueId();
    } else {
      return Sponge.server().scheduler().submit(task).uniqueId();
    }
  }

  @Override
  public void cancelTask(UUID taskId) {
    Sponge.asyncScheduler().findTask(taskId).ifPresent(ScheduledTask::cancel);
    Sponge.server().scheduler().findTask(taskId).ifPresent(ScheduledTask::cancel);
  }

  @Override
  public boolean isMainThread() {
    return Sponge.server().onMainThread();
  }
}
