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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import net.whimxiqal.journey.Journey;

/**
 * A manager that distributes work over potentially multiple threads
 */
public class DistributedWorkManager {

  private final int maxActiveWorkItems;

  private final Object lock = new Object();
  // number of active work items per owner
  private final Map<UUID, Integer> ownerActiveCountMap = new HashMap<>();
  private final Map<UUID, LinkedList<WorkItemExecutor>> workReplacementMap = new HashMap<>();
  private int activeWorkItems = 0;

  public DistributedWorkManager(int maxActiveWorkItems) {
    if (maxActiveWorkItems < 1) {
      throw new IllegalArgumentException();
    }
    this.maxActiveWorkItems = maxActiveWorkItems;
  }

  public void schedule(WorkItem work) {
    execute(new WorkItemExecutor(this, work));
  }

  private void execute(WorkItemExecutor executor) {
    Journey.get().proxy().schedulingManager().schedule(executor, true);
  }

  /**
   * Helper class to execute {@link WorkItem}.
   */
  private static class WorkItemExecutor implements Runnable {

    private final DistributedWorkManager manager;
    private final WorkItem work;
    private boolean active;

    public WorkItemExecutor(DistributedWorkManager manager, WorkItem work) {
      this.manager = manager;
      this.work = work;
      this.active = false;
    }

    @Override
    public void run() {
      // Do preliminary management of active work.
      //  We want to make sure that we never have too many active work items executing at once
      //  since each work item can be using a lot of memory at once,
      //  and we also want to make sure that new work items can request active "slots" if a single owner
      //  is using too many of them.
      WorkItemExecutor target = this;
      synchronized (manager.lock) {
        if (active) {
          // We are active. Was a replacement scheduled?
          LinkedList<WorkItemExecutor> replacements = manager.workReplacementMap.get(work.owner());
          if (replacements != null) {
            WorkItemExecutor replacement = replacements.poll();
            assert (replacement != null);  // replacements must be non-empty
            if (replacements.isEmpty()) {
              // cleanup list from map
              manager.workReplacementMap.remove(work.owner());
            }
            replacement.setActive();
            this.setInactive();
            this.work.reset();

            target = replacement;
            manager.execute(this);  // schedule this again, even though we're not active anymore
            // continue with execution, but with the replacement instead
          }
        } else {
          // Not active yet. Can we set to active?
          if (manager.activeWorkItems > manager.maxActiveWorkItems) {
            throw new IllegalStateException();  // we should never have more active work items than allowed
          }
          if (manager.activeWorkItems == manager.maxActiveWorkItems) {
            // We have reached our limit of active work items. Try to acquire a slot from another active one.
            attemptAcquire();
            return;
          }
          // We have not reached the maximum number of active work items yet. Set to active and continue
          setActive();
        }
      }

      // Standard execution.
      boolean done = target.work.run();

      if (done) {
        synchronized (manager.lock) {
          boolean targetOwnerDone = target.setInactive();
          if (targetOwnerDone) {
            // Some replacements may have been queued while this target was running.
            // But, this owner is done, so there are no more opportunities for any scheduled replacements to run.
            // Just requeue them, and set the first one to active
            LinkedList<WorkItemExecutor> replacements = manager.workReplacementMap.remove(target.work.owner());
            if (replacements != null) {
              boolean usedSlot = false;
              for (WorkItemExecutor replacement : replacements) {
                if (!usedSlot) {
                  replacement.setActive();
                  usedSlot = true;
                }
                manager.execute(replacement);
              }
            }
          }
        }
      } else {
        // Work is not done. Re-schedule.
        manager.execute(target);
      }
    }

    private void setActive() {
      active = true;
      manager.activeWorkItems++;
      manager.ownerActiveCountMap.merge(work.owner(), 1, Integer::sum);
    }

    /**
     * Sets the work executor as inactive, and returns whether the
     * owner is done with all its active work.
     *
     * @return true if the owner has no more active work, false if there is still some active work
     */
    private boolean setInactive() {
      active = false;
      manager.activeWorkItems--;
      int activeCount = manager.ownerActiveCountMap.get(work.owner());
      if (activeCount == 1) {
        manager.ownerActiveCountMap.remove(work.owner());
        return true;
      } else {
        manager.ownerActiveCountMap.put(work.owner(), activeCount - 1);
        return false;
      }
    }

    /**
     * Try to schedule the work anyway, considering that other owners may have active work going on.
     */
    private void attemptAcquire() {
      Integer activeItems = manager.ownerActiveCountMap.get(work.owner());
      if (activeItems != null && activeItems > 0) {
        // This owner has other active work, so there no pressing need for this work. Let's just reschedule this for later
        manager.execute(this);
        return;
      }

      // This owner has no active work yet. Let's try to steal a slot from the most active owner
      UUID maxOwner = null;
      int maxItems = 0;
      for (Map.Entry<UUID, Integer> ownerActiveCount : manager.ownerActiveCountMap.entrySet()) {
        int consideredActive = ownerActiveCount.getValue();
        if (manager.workReplacementMap.containsKey(ownerActiveCount.getKey())) {
          // We already have replacement work scheduled for this owner, so subtract that from consideration here
          consideredActive -= manager.workReplacementMap.get(ownerActiveCount.getKey()).size();
        }
        if (consideredActive > maxItems) {
          maxOwner = ownerActiveCount.getKey();
          maxItems = ownerActiveCount.getValue();
        }
      }
      assert (maxOwner != null);  // There must be at least one owner that has active items
      assert (maxItems > 0);
      if (maxItems == 1) {
        // The most active owner has only 1 work item active, so shouldn't steal a slot from anyone
        manager.execute(this);
        return;
      }

      // The most active owner has more than 1 active work item, so we can steal one and the owner can still be doing work
      manager.workReplacementMap.computeIfAbsent(maxOwner, k -> new LinkedList<>()).add(this);
    }
  }
}
