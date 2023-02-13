package net.whimxiqal.journey.stats;

import java.util.UUID;
import net.whimxiqal.journey.Journey;

public class StatsManager {

  private static final int TICK_PERIOD = 20 * 60 * 60;  // one hour
  private UUID task;
  private int searches = 0;
  private double blocksTravelled = 0;

  private int storedSearches = 0;
  private int storedBlocksTravelled = 0;

  public void initialize() {
    if (task != null) {
      throw new IllegalStateException("We're already initialized");
    }
    reset();
    task = Journey.get().proxy().schedulingManager().scheduleRepeat(() -> {
        store();
        reset();
    }, false, TICK_PERIOD);
  }

  private synchronized void store() {
    storedSearches = searches;
    storedBlocksTravelled = (int) Math.floor(blocksTravelled);
  }

  private synchronized void reset() {
    searches = 0;
    blocksTravelled = 0;
  }

  /**
   * Increment the number of searches that have occurred. Thread-safe.
   */
  public synchronized void incrementSearches() {
    searches++;
  }

  /**
   * Get the number of searches in the last hour. Thread-safe.
   * @return searches
   */
  public synchronized int searches() {
    return storedSearches;
  }

  /**
   * Increment the number of blocks that have been travelled. Thread-safe.
   */
  public synchronized void addBlocksTravelled(double blocks) {
    blocksTravelled += blocks;
  }

  /**
   * Get the number of blocks travelled in the last hour. Thread-safe.
   * @return blocks travelled
   */
  public synchronized int blocksTravelled() {
    return storedBlocksTravelled;
  }

  public void shutdown() {
    if (task != null) {
      Journey.get().proxy().schedulingManager().cancelTask(task);
      task = null;
    }
  }

}
