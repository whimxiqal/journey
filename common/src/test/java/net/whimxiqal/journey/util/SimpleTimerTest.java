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
    Assertions.assertTrue(timer.elapsed() > 10);
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