package net.whimxiqal.journey.util;

public class SimpleTimer {

  private long executionStartTime = -1;

  public void start() {
    executionStartTime = System.currentTimeMillis();
  }

  public long elapsed() {
    if (executionStartTime < 0) {
      return -1;
    }
    return System.currentTimeMillis() - executionStartTime;
  }

}
