package net.whimxiqal.journey.manager;

import java.util.UUID;

public class TestWorkItem implements WorkItem {
  final UUID owner;
  final int target;
  final Runnable runOnRun;
  boolean stall = false;
  int counter = 0;

  public TestWorkItem(UUID owner, int target, Runnable runOnRun) {
    this.owner = owner;
    this.target = target;
    this.runOnRun = runOnRun;
  }

  @Override
  public UUID owner() {
    return owner;
  }

  @Override
  public boolean run() {
    runOnRun.run();
    if (stall) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    counter++;
    assert (counter <= target);
    return counter == target;
  }

  public boolean done() {
    return counter == target;
  }

  @Override
  public void reset() {
    counter = 0;
  }

  public TestWorkItem stall() {
    this.stall = true;
    return this;
  }
}
