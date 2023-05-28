package net.whimxiqal.journey.manager;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DistributedWorkManagerTest {

  static final LinkedList<Integer> ordering = new LinkedList<>();
  static final LinkedList<TestWorkItem> workItems = new LinkedList<>();

  @BeforeEach
  void setUp() {
    ordering.clear();
    workItems.clear();
  }

  TestWorkItem genWorkItem(int id, UUID owner, int target) {
    TestWorkItem item = new TestWorkItem(owner, target, () -> {
      synchronized (ordering) {
        ordering.add(id);
      }
    });
    workItems.add(item);
    return item;
  }

  boolean done() {
    workItems.removeIf(TestWorkItem::done);
    return workItems.isEmpty();
  }

  void verifyOrdering(int[][] ids) {
    Assertions.assertEquals(ids.length, ordering.size());
    int idx = 0;
    for (int id : ordering) {
      if (id == -1) {
        continue;
      }
      boolean found = false;
      for (int option : ids[idx]) {
        if (option == id) {
          found = true;
          break;
        }
      }
      Assertions.assertTrue(found, "Ordering at index " + idx + " is incorrect. It was "
          + id + " but should have been one of "
          + Arrays.toString(ids[idx]));
      idx++;
    }
  }

  @Test
  void singleThreaded() throws InterruptedException {
    DistributedWorkManager manager = new DistributedWorkManager(1, 2);

    UUID owner0 = UUID.randomUUID();
    UUID owner1 = UUID.randomUUID();

    manager.schedule(genWorkItem(0, owner0, 3).stall());
    manager.schedule(genWorkItem(1, owner0, 3).stall());

    while (ordering.size() < 1) {
      Thread.sleep(10);
    }

    // This shouldn't run at all at first, since it's still the same owner and active items are maxed out
    manager.schedule(genWorkItem(2, owner0, 3).stall());

    // This should kick out item0 or item1
    manager.schedule(genWorkItem(3, owner1, 3).stall());

    while (!done()) {
      Thread.sleep(100);
    }
    verifyOrdering(new int[][]{
        {0},
        {1},
        {3}, // deactivates 0 or 1 (and resets it)
        {0, 1}, // is either 0 or 1
        {3},
        {0, 1}, // is either 0 or 1, and is now complete
        {0, 1, 2, 3}, // the other of 0 or 1, or 2 since 0 or 1 has completed, or it could be 3
        {0, 1, 2, 3}, // 3 must be completed by now, if not completed the time before
        {0, 1, 2},
        {0, 1, 2},
        {0, 1, 2},
        {0, 1, 2},
        {0, 1, 2},
    });  // 13 total because 0 or 1 had to be reset when 3 deactivated it
  }

  @Test
  void singleActiveWorkItem() throws InterruptedException {
    DistributedWorkManager manager = new DistributedWorkManager(3, 1);

    UUID owner0 = UUID.randomUUID();

    manager.schedule(genWorkItem(0, owner0, 2).stall());
    manager.schedule(genWorkItem(1, owner0, 2).stall());
    manager.schedule(genWorkItem(2, owner0, 2).stall());

    while (!done()) {
      Thread.sleep(100);
    }
    for (int i = 0; i < ordering.size(); i += 2) {
      Assertions.assertEquals(ordering.get(i), ordering.get(i + 1), "Ordering at index " + i + " was not the same as the next one");
    }
  }

  @Test
  void manyTasks() throws InterruptedException {
    DistributedWorkManager manager = new DistributedWorkManager(3, 2);

    UUID owner0 = UUID.randomUUID();

    int N = 100;
    for (int i = 0; i < N; i++) {
      manager.schedule(genWorkItem(i, owner0, i + 1));
    }

    while (!done()) {
      Thread.sleep(100);
    }

    int totalRuns = (N + 1) * N / 2;
    Assertions.assertEquals(totalRuns, ordering.size());
  }

}