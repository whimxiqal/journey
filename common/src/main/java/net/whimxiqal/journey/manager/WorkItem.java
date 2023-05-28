package net.whimxiqal.journey.manager;

import java.util.UUID;

public interface WorkItem {

  /**
   * The id of the owner of this work, for load balancing.
   */
  UUID owner();

  /**
   * Execute work.
   *
   * @return true if done, false if there is more work to do
   */
  boolean run();

  /**
   * Reset all state on this object, and clear any cached data.
   * Run may be re-run on this object, and it would be as if it was called for the first time.
   */
  void reset();

}
