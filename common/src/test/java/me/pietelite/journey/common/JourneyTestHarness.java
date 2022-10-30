package me.pietelite.journey.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
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
    Journey.get().registerProxy(new TestProxy());
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
