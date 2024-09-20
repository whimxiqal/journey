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

package net.whimxiqal.journey.stats;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class RateStatistic {
  private final long periodMs;
  private final Queue<Node> queue = new LinkedList<>();
  private final AtomicReference<Double> accumulator = new AtomicReference<>();
  private final Supplier<Long> currentTime;
  private double queueSum;

  private record Node(double data, long timestamp) {
  }

  /**
   * General constructor.
   *
   * @param periodMs the period of time to take before data is rolled off
   */
  public RateStatistic(long periodMs, Supplier<Long> currentTime) {
    this.periodMs = periodMs;
    this.currentTime = currentTime;
    accumulator.set(0.0);
  }

  public RateStatistic(long periodMs) {
    this(periodMs, System::currentTimeMillis);
  }

  private void prune(long now) {
    while (!queue.isEmpty() && now - queue.peek().timestamp >= periodMs) {
      Node node = queue.remove();
      queueSum -= node.data;
    }
  }

  public void store() {
    long now = currentTime.get();
    prune(now);
    double accumulated = accumulator.getAndSet(0.0);
    queue.add(new Node(accumulated, now));
    queueSum += accumulated;
  }

  public void add(double value) {
    accumulator.getAndAccumulate(value, Double::sum);
  }

  public double getInPeriod() {
    prune(currentTime.get());
    return queueSum;
  }

  public int getIntInPeriod() {
    return (int) getInPeriod();
  }
}
