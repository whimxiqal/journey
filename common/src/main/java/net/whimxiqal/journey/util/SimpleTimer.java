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

public class SimpleTimer {

  private long startTime = -1;
  private long stopTime = -1;

  /**
   * Start or re-start the timer.
   */
  public void start() {
    synchronized (this) {
      startTime = System.currentTimeMillis();
      stopTime = -1;
    }
  }

  /**
   * Stop the timer, so the current elapsed time is saved.
   */
  public void stop() {
    synchronized (this) {
      stopTime = System.currentTimeMillis();
    }
  }

  public long elapsed() {
    synchronized (this) {
      if (startTime < 0) {
        // not yet started
        return -1;
      }
      long now = System.currentTimeMillis();
      if (stopTime < startTime) {
        return now - startTime;
      }
      return stopTime - startTime;
    }
  }

}
