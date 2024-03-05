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

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import net.whimxiqal.journey.Journey;

/**
 * A generic interface for common logging purposes.
 */
public abstract class CommonLogger {

  private static final int MAX_LOGS_PER_TICK = 1000;
  private final Queue<Message> messageQueue = new ConcurrentLinkedQueue<>();
  private final AtomicReference<LogLevel> logLevel = new AtomicReference<>(LogLevel.INFO);
  private UUID messageTaskId;
  private boolean immediateSubmit = false;

  abstract protected void submit(Message message);

  /**
   * Log something at the info-level.
   *
   * @param message the message
   */
  public void info(String message) {
    log(LogLevel.INFO, message);
  }

  /**
   * Log something at the warn-level.
   */
  public void warn(String message) {
    log(LogLevel.WARNING, message);
  }

  /**
   * Log something at the sever-level.
   */
  public void error(String message) {
    log(LogLevel.SEVERE, message);
  }

  public void debug(String message) {
    log(LogLevel.DEBUG, message);
  }

  public LogLevel level() {
    return logLevel.get();
  }

  /**
   * Sets the level, so any messages that have a greater level are ignored.
   *
   * @param level the level
   */
  public void setLevel(LogLevel level) {
    logLevel.set(level);
  }

  private void log(LogLevel type, String message) {
    if (type.level <= logLevel.get().level) {
      // only queue this log if the level is below our allowed log level
      Message msg = new Message(type, message);
      if (immediateSubmit) {
        submit(msg);
      } else {
        messageQueue.add(msg);
      }
    }
  }

  public void initialize() {
    messageTaskId = Journey.get().proxy().schedulingManager().scheduleRepeat(this::flush, false, 1);
  }

  public void shutdown() {
    if (messageTaskId != null) {
      Journey.get().proxy().schedulingManager().cancelTask(messageTaskId);
    }
    flush();
  }

  public void setImmediateSubmit(boolean immediateSubmit) {
    this.immediateSubmit = immediateSubmit;
  }

  public void flush() {
    int count = 0;
    while (!messageQueue.isEmpty()) {
      submit(messageQueue.remove());
      count++;
      if (count > MAX_LOGS_PER_TICK) {
        submit(new Message(LogLevel.WARNING, String.format("[Logger] Truncated %d logs", messageQueue.size())));
        messageQueue.clear();
        break;
      }
    }
  }

  public enum LogLevel {
    SEVERE("SEVER", 0),
    WARNING("WARN ", 1),
    INFO("INFO ", 2),
    DEBUG("DEBUG", 3);

    private final String label;
    private final int level;

    LogLevel(String label, int level) {
      this.label = label;
      this.level = level;
    }

    public String label() {
      return label;
    }

    public int level() {
      return level;
    }
  }

  protected record Message(LogLevel type, String message) {
    @Override
    public String toString() {
      return "[" + type.label + "] " + message;
    }
  }

}
