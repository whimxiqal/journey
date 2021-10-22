/*
 * Copyright 2021 Pieter Svenson
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

package edu.whimc.indicator.common.search.event;

import edu.whimc.indicator.common.navigation.Cell;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public class SearchDispatcher<T extends Cell<T, D>, D, E> {

  protected final Map<SearchEvent.EventType, SearchEventConversion<?, E>> events = new HashMap<>();
  private final Consumer<E> externalDispatcher;

  public SearchDispatcher(@NotNull Consumer<E> externalDispatcher) {
    this.externalDispatcher = externalDispatcher;
  }

  public final <S extends SearchEvent<T, D>> void registerEvent(SearchEventConversion<S, E> eventConversion,
                                                                SearchEvent.EventType eventType) {
    events.put(eventType, eventConversion);
  }

  private <S extends SearchEvent<T, D>> SearchEventConversion<S, E> getEventConversion(SearchEvent.EventType eventType) {
    return (SearchEventConversion<S, E>) events.get(eventType);
  }

  /**
   * A method used to dispatch search events throughout the operation of
   * the search method in this class. Ultimately, a counterpart to this
   * event will be dispatched to the appropriate event handling system implemented
   * in a given Minecraft mod, e.g. Bukkit/Spigot's event handling system.
   *
   * @param event      a search event
   * @param <S>        the type of event
   */
  public final <S extends SearchEvent<T, D>> void dispatch(S event) {
    if (events.containsKey(event.type())) {
      this.externalDispatcher.accept(getEventConversion(event.type()).convert(event));
    } else {
      throw new IllegalArgumentException("An event of type " + event.type() + " has no conversion.");
    }
  }

}
