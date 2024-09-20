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

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RateStatisticTest {

  @Test
  void testStatistic() {
    AtomicReference<Long> currentTime = new AtomicReference<>(0L);
    RateStatistic stat = new RateStatistic(1000, currentTime::get);  // one second
    stat.add(10.0);
    Assertions.assertEquals(0.0, stat.getInPeriod());  // 0 ms
    stat.store();
    Assertions.assertEquals(10.0, stat.getInPeriod());  // 0 ms
    stat.add(20.0);
    stat.store();
    Assertions.assertEquals(30.0, stat.getInPeriod());  // 0 ms
    currentTime.set(500L);

    stat.add(2.0);
    stat.add(3.0);
    Assertions.assertEquals(30.0, stat.getInPeriod());  // 500 ms
    stat.store();
    Assertions.assertEquals(35.0, stat.getInPeriod());  // 500 ms
    currentTime.set(1000L);

    Assertions.assertEquals(5.0, stat.getInPeriod());  // 1000 ms
    currentTime.set(1500L);

    Assertions.assertEquals(0.0, stat.getInPeriod());  // 1500 ms
  }

}
