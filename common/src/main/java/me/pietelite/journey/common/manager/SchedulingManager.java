package me.pietelite.journey.common.manager;

import java.util.UUID;

public interface SchedulingManager {

  void schedule(Runnable runnable, boolean async);

  void schedule(Runnable runnable, boolean async, int tickDelay);

  UUID scheduleRepeat(Runnable runnable, boolean async, int tickPeriod);

  void cancelTask(UUID taskId);

}
