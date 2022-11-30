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

package me.pietelite.journey.common.search.event;

import java.util.UUID;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.util.Initializable;

public final class SearchDispatcherImpl extends SearchDispatcher implements Initializable {
  private UUID dispatchTaskUuid;

  @Override
  public void initialize() {
    dispatchTaskUuid = Journey.get().proxy().schedulingManager().scheduleRepeat(() -> {
      while (!events.isEmpty()) {
        dispatchHelper(events.remove());
      }
    }, false, 1);
  }

  /**
   * A method used to dispatch search events throughout the operation of
   * the search method in this class. Ultimately, a counterpart to this
   * event will be dispatched to the appropriate event handling system implemented
   * in a given Minecraft mod, e.g. Bukkit/Spigot's event handling system.
   *
   * @param event a search event
   * @param <S>   the type of event
   */
  private <S extends SearchEvent> void dispatchHelper(S event) {
    if (eventConversions.containsKey(event.type())) {
      this.externalDispatcher.accept(eventConversions.get(event.type()).convert(event));
    } else {
      this.externalDispatcher.accept(event);
    }
  }

  public void shutdown() {
    Journey.get().proxy().schedulingManager().cancelTask(dispatchTaskUuid);
  }

}
