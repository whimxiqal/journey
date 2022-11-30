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

package me.pietelite.journey.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import me.pietelite.journey.common.data.TestDataManager;
import me.pietelite.journey.common.navigation.Itinerary;
import me.pietelite.journey.common.search.event.FoundSolutionEvent;
import me.pietelite.journey.common.search.event.SearchDispatcher;
import me.pietelite.journey.common.search.event.SearchEvent;
import me.pietelite.journey.common.search.event.StartItinerarySearchEvent;
import me.pietelite.journey.common.search.event.StartPathSearchEvent;
import me.pietelite.journey.common.search.event.StepSearchEvent;
import me.pietelite.journey.common.search.event.StopItinerarySearchEvent;
import me.pietelite.journey.platform.WorldLoader;
import org.junit.jupiter.api.BeforeAll;

public class JourneyTestHarness {

  protected static final boolean DEBUG = false;
  protected static final Map<Class<SearchEvent>, Consumer<SearchEvent>> eventRunners = new HashMap<>();
  protected static Map<UUID, Itinerary> sessionItineraries = new HashMap<>();

  static <E extends SearchEvent> void registerEvent(Class<E> clazz, Consumer<E> runner) {
    eventRunners.put((Class<SearchEvent>) clazz, (Consumer<SearchEvent>) runner);
  }

  @BeforeAll
  static void setupWorlds() {
    if (Journey.get().proxy() == null) {
      Journey.get().registerProxy(new TestProxy());
      Journey.get().setDataManager(new TestDataManager());
      Journey.get().debugManager().setConsoleDebugging(DEBUG);
      WorldLoader.initWorlds();

      SearchDispatcher.Editor<SearchEvent> dispatcher = Journey.get().dispatcher().editor();
      registerEvent(FoundSolutionEvent.class, event -> sessionItineraries.put(event.getSession().uuid(), event.getItinerary()));
      registerEvent(StepSearchEvent.class, event -> Journey.logger().debug("StepSearchEvent: " + event.getStep().toString()));
      registerEvent(StartItinerarySearchEvent.class, event -> Journey.logger().debug("StartItinerarySearchEvent"));
      registerEvent(StopItinerarySearchEvent.class, event -> Journey.logger().debug("StopItinerarySearchEvent"));
      registerEvent(StartPathSearchEvent.class, event -> Journey.logger().debug("StartPathSearchEvent"));
      dispatcher.setExternalDispatcher(testEvent -> {
        Consumer<SearchEvent> runner = eventRunners.get(testEvent.getClass());
        if (runner != null) {
          runner.accept(testEvent);
        }
      });
    }
  }

}
