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
import java.util.concurrent.ConcurrentLinkedQueue;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.search.SearchSession;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A dispatcher of events used during the execution of a
 * {@link SearchSession}.
 */
public abstract class SearchDispatcher {

  protected final Map<SearchEvent.EventType, SearchEventConversion<SearchEvent, Object>> eventConversions = new HashMap<>();
  protected Consumer<Object> externalDispatcher;
  private boolean edited = false;
  protected final ConcurrentLinkedQueue<SearchEvent> events = new ConcurrentLinkedQueue<>();

  public <E> Editor<E> editor() {
    if (edited) {
      throw new RuntimeException("You should not be editing the search dispatcher after initialization");
    }
    edited = true;
    return new Editor<>(this);
  }

  public <S extends SearchEvent> void dispatch(S event) {
    events.add(event);
  }

  public final static class Editor<E> {
    private final SearchDispatcher dispatcher;

    public Editor(SearchDispatcher dispatcher) {
      this.dispatcher = dispatcher;
    }

    public void setExternalDispatcher(Consumer<E> externalDispatcher) {
      dispatcher.externalDispatcher = (Consumer<Object>) externalDispatcher;
    }

    public <S extends SearchEvent> void registerEvent(SearchEventConversion<S, E> eventConversion,
                                                            SearchEvent.EventType eventType) {
      dispatcher.eventConversions.put(eventType, (SearchEventConversion<SearchEvent, Object>) eventConversion);
    }

  }

}