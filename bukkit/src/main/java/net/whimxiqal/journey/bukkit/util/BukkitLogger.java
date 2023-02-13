/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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

package net.whimxiqal.journey.bukkit.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import net.whimxiqal.journey.util.CommonLogger;
import net.whimxiqal.journey.bukkit.JourneyBukkit;
import org.bukkit.Bukkit;

/**
 * An implementation the simple common Journey logger.
 */
public class BukkitLogger implements CommonLogger {

  Queue<Message> messageQueue = new ConcurrentLinkedQueue<>();

  enum MessageType {
    INFO,
    WARNING,
    SEVERE,
  }

  private static class Message {
    MessageType type;
    String message;
    public Message(MessageType type, String message) {
      this.type = type;
      this.message = message;
    }
  }

  @Override
  public void info(String message) {
    log(MessageType.INFO, message);
  }

  @Override
  public void warn(String message) {
    log(MessageType.WARNING, message);
  }

  @Override
  public void error(String message) {
    log(MessageType.SEVERE, message);
  }

  private void log(MessageType type, String message) {
    messageQueue.add(new Message(type, message));
  }

  public void init() {
    Bukkit.getScheduler().runTaskTimer(JourneyBukkit.get(), () -> {
      while (!messageQueue.isEmpty()) {
        Message message = messageQueue.remove();
        Logger logger = JourneyBukkit.get().getLogger();
        switch (message.type) {
          case WARNING:
            logger.warning(message.message);
            break;
          case SEVERE:
            logger.severe(message.message);
            break;
          case INFO:
          default:
            logger.info(message.message);
        }
      }
    }, 0, 1);
  }
}
