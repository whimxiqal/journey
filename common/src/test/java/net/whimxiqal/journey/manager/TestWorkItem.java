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
