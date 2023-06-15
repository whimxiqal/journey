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

package net.whimxiqal.journey.bukkit.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.whimxiqal.journey.manager.SchedulingManager;
import net.whimxiqal.journey.bukkit.JourneyBukkit;
import org.bukkit.Bukkit;

public class BukkitSchedulingManager implements SchedulingManager {

  private final Map<UUID, Integer> uuidToId = new HashMap<>();

  @Override
  public void schedule(Runnable runnable, boolean async) {
    if (async) {
      Bukkit.getScheduler().runTaskAsynchronously(JourneyBukkit.get(), runnable);
    } else {
      Bukkit.getScheduler().runTask(JourneyBukkit.get(), runnable);
    }
  }

  @Override
  public void schedule(Runnable runnable, boolean async, int tickDelay) {
    if (tickDelay <= 0) {
      schedule(runnable, async);
    }
    if (async) {
      Bukkit.getScheduler().runTaskLaterAsynchronously(JourneyBukkit.get(), runnable, tickDelay);
    } else {
      Bukkit.getScheduler().runTaskLater(JourneyBukkit.get(), runnable, tickDelay);
    }
  }

  @Override
  public UUID scheduleRepeat(Runnable runnable, boolean async, int tickPeriod) {
    int id;
    if (async) {
      id = Bukkit.getScheduler().runTaskTimerAsynchronously(JourneyBukkit.get(), runnable, 0, tickPeriod).getTaskId();
    } else {
      id = Bukkit.getScheduler().runTaskTimer(JourneyBukkit.get(), runnable, 0, tickPeriod).getTaskId();
    }
    UUID uuid = UUID.randomUUID();
    uuidToId.put(uuid, id);
    return uuid;
  }

  @Override
  public void cancelTask(UUID taskId) {
    Integer id = uuidToId.get(taskId);
    if (id == null) {
      return;
    }
    Bukkit.getScheduler().cancelTask(id);
  }

  @Override
  public boolean isMainThread() {
    return Bukkit.isPrimaryThread();
  }
}
