package net.whimxiqal.journey.bukkit.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.whimxiqal.journey.common.manager.SchedulingManager;
import net.whimxiqal.journey.bukkit.JourneyBukkit;
import org.bukkit.Bukkit;

public class BukkitSchedulingManager implements SchedulingManager {

  private final Map<UUID, Integer> uuidToId = new HashMap<>();

  @Override
  public void schedule(Runnable runnable, boolean async) {
    if (async) {
      Bukkit.getScheduler().runTaskAsynchronously(JourneyBukkit.getInstance(), runnable);
    } else {
      Bukkit.getScheduler().runTask(JourneyBukkit.getInstance(), runnable);
    }
  }

  @Override
  public void schedule(Runnable runnable, boolean async, int tickDelay) {
    if (tickDelay <= 0) {
      schedule(runnable, async);
    }
    if (async) {
      Bukkit.getScheduler().runTaskLaterAsynchronously(JourneyBukkit.getInstance(), runnable, tickDelay);
    } else {
      Bukkit.getScheduler().runTaskLater(JourneyBukkit.getInstance(), runnable, tickDelay);
    }
  }

  @Override
  public UUID scheduleRepeat(Runnable runnable, boolean async, int tickPeriod) {
    int id;
    if (async) {
      id = Bukkit.getScheduler().runTaskTimerAsynchronously(JourneyBukkit.getInstance(), runnable, 0, tickPeriod).getTaskId();
    } else {
      id = Bukkit.getScheduler().runTaskTimer(JourneyBukkit.getInstance(), runnable, 0, tickPeriod).getTaskId();
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
}
