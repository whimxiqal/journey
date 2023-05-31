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

package net.whimxiqal.journey.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleTimerTest {

  @Test
  public void testStartElapsed() throws InterruptedException {
    SimpleTimer timer = new SimpleTimer();
    timer.start();
    Thread.sleep(10);
    Assertions.assertTrue(timer.elapsed() >= 10);
  }

  @Test
  public void testStartStopElapsed() throws InterruptedException {
    SimpleTimer timer = new SimpleTimer();
    timer.start();
    Thread.sleep(10);
    timer.stop();
    Thread.sleep(100);
    Assertions.assertTrue(timer.elapsed() >= 0);
    Assertions.assertTrue(timer.elapsed() < 100);
  }

  @Test
  public void testRestart() throws InterruptedException {
    SimpleTimer timer = new SimpleTimer();
    timer.start();
    Thread.sleep(10);
    timer.start();
    Assertions.assertTrue(timer.elapsed() >= 0);
    Assertions.assertTrue(timer.elapsed() < 10);
  }

  @Test
  public void unStarted() {
    SimpleTimer timer = new SimpleTimer();
    Assertions.assertTrue(timer.elapsed() < 0);
  }

}