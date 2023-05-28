package net.whimxiqal.journey.manager;

import java.util.concurrent.ThreadFactory;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link ThreadFactory} that produces threads with names containing "Journey".
 */
public class WorkThreadFactory implements ThreadFactory {
  @Override
  public Thread newThread(@NotNull Runnable r) {
    Thread thread = new Thread(r);
    thread.setName("Journey Worker (" + thread.getId() + ")");
    return thread;
  }
}
