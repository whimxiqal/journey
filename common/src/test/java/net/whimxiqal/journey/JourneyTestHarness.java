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

package net.whimxiqal.journey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.data.TestDataManager;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.platform.TestJourneyPlayer;
import net.whimxiqal.journey.platform.TestPlatformProxy;
import net.whimxiqal.journey.search.event.FoundSolutionEvent;
import net.whimxiqal.journey.search.event.SearchDispatcher;
import net.whimxiqal.journey.search.event.SearchEvent;
import net.whimxiqal.journey.search.event.StartItinerarySearchEvent;
import net.whimxiqal.journey.search.event.StartPathSearchEvent;
import net.whimxiqal.journey.search.event.StartSearchEvent;
import net.whimxiqal.journey.search.event.StopItinerarySearchEvent;
import net.whimxiqal.journey.platform.WorldLoader;
import net.whimxiqal.journey.search.event.StopPathSearchEvent;
import net.whimxiqal.journey.search.event.StopSearchEvent;
import org.junit.jupiter.api.BeforeAll;

public class JourneyTestHarness {

  protected static final boolean DEBUG = false;
  protected static final Map<Class<SearchEvent>, Consumer<SearchEvent>> eventRunners = new HashMap<>();
  protected static final UUID PLAYER_UUID = UUID.randomUUID();
  protected static final Map<UUID, Itinerary> SESSION_ITINERARIES = new HashMap<>();
  private static boolean initialized = false;

  @SuppressWarnings("unchecked")
  static <E extends SearchEvent> void registerEvent(Class<E> clazz, Consumer<E> runner) {
    eventRunners.put((Class<SearchEvent>) clazz, (Consumer<SearchEvent>) runner);
  }

  @BeforeAll
  static void initializeHarness() {
    if (initialized) {
      return;
    }
    initialized = true;

    Settings.DEDICATED_THREADS.setValue(1);
    Settings.MAX_SEARCHES.setValue(10);

    TestProxy proxy = new TestProxy();
    Journey.get().registerProxy(proxy);
    proxy.schedulingManager.startMainThread();
    Journey.get().setDataManager(new TestDataManager());
    Journey.get().debugManager().setConsoleDebugging(DEBUG);
    Journey.get().init();
    WorldLoader.initWorlds();

    TestPlatformProxy.onlinePlayers.add(new TestJourneyPlayer(PLAYER_UUID));
    Journey.get().tunnelManager().register(player -> TestPlatformProxy.tunnels);

    SearchDispatcher.Editor<SearchEvent> dispatcher = Journey.get().dispatcher().editor();
    registerEvent(FoundSolutionEvent.class, event -> {
      SESSION_ITINERARIES.put(event.getSession().uuid(), event.getItinerary());
    });
    registerEvent(StartSearchEvent.class, event -> Journey.get().debugManager().broadcast(Component.text(event.toString())));
    registerEvent(StopSearchEvent.class, event -> Journey.get().debugManager().broadcast(Component.text(event.toString())));
    registerEvent(StartItinerarySearchEvent.class, event -> Journey.get().debugManager().broadcast(Component.text(event.toString())));
    registerEvent(StopItinerarySearchEvent.class, event -> Journey.get().debugManager().broadcast(Component.text(event.toString())));
    registerEvent(StartPathSearchEvent.class, event -> Journey.get().debugManager().broadcast(Component.text(event.toString())));
    registerEvent(StopPathSearchEvent.class, event -> Journey.get().debugManager().broadcast(Component.text(event.toString())));
    dispatcher.setExternalDispatcher(testEvent -> {
      Consumer<SearchEvent> runner = eventRunners.get(testEvent.getClass());
      if (runner != null) {
        runner.accept(testEvent);
      }
    });
  }

}
