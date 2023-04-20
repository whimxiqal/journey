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
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.Journey;

/**
 * A generic interface for common logging purposes.
 */
public abstract class CommonLogger implements Initializable {

  private final Queue<Message> messageQueue = new ConcurrentLinkedQueue<>();
  private UUID messageTaskId;

  abstract protected void submit(Message message);

  /**
   * Log something at the info-level.
   *
   * @param message the message
   */
  public void info(String message) {
    log(MessageType.INFO, message);
  }

  /**
   * Log something at the warn-level.
   */
  public void warn(String message) {
    log(MessageType.WARNING, message);
  }

  /**
   * Log something at the sever-level.
   */
  public void error(String message) {
    log(MessageType.SEVERE, message);
  }

  public void debug(String message) {
    debug(Component.text(message));
  }

  public void debug(Component message) {
    Journey.get().debugManager().broadcast(message);
  }

  private void log(MessageType type, String message) {
    messageQueue.add(new Message(type, message));
  }

  @Override
  public void initialize() {
    messageTaskId = Journey.get().proxy().schedulingManager().scheduleRepeat(this::flush, false, 1);
  }

  public void shutdown() {
    if (messageTaskId != null) {
      Journey.get().proxy().schedulingManager().cancelTask(messageTaskId);
    }
    flush();
  }

  private void flush() {
    while (!messageQueue.isEmpty()) {
      submit(messageQueue.remove());
    }
  }

  protected enum MessageType {
    INFO,
    WARNING,
    SEVERE,
  }

  protected record Message(MessageType type, String message) {
  }

}
