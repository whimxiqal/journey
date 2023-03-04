package net.whimxiqal.journey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.whimxiqal.journey.data.TestDataManager;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.search.event.FoundSolutionEvent;
import net.whimxiqal.journey.search.event.SearchDispatcher;
import net.whimxiqal.journey.search.event.SearchEvent;
import net.whimxiqal.journey.search.event.StartItinerarySearchEvent;
import net.whimxiqal.journey.search.event.StartPathSearchEvent;
import net.whimxiqal.journey.search.event.StepSearchEvent;
import net.whimxiqal.journey.search.event.StopItinerarySearchEvent;
import net.whimxiqal.journey.platform.WorldLoader;
import org.junit.jupiter.api.BeforeAll;

public class JourneyTestHarness {

  protected static final boolean DEBUG = false;
  protected static final Map<Class<SearchEvent>, Consumer<SearchEvent>> eventRunners = new HashMap<>();
  protected static Map<UUID, Itinerary> sessionItineraries = new HashMap<>();

  @SuppressWarnings("unchecked")
  static <E extends SearchEvent> void registerEvent(Class<E> clazz, Consumer<E> runner) {
    eventRunners.put((Class<SearchEvent>) clazz, (Consumer<SearchEvent>) runner);
  }

  @BeforeAll
  static void setupWorlds() {
    if (Journey.get().proxy() != null) {
      return;
    }
    TestProxy proxy = new TestProxy();
    Journey.get().registerProxy(proxy);
    proxy.schedulingManager.startMainThread();
    Journey.get().setDataManager(new TestDataManager());
    Journey.get().debugManager().setConsoleDebugging(DEBUG);
    Journey.get().init();
    WorldLoader.initWorlds();

    SearchDispatcher.Editor<SearchEvent> dispatcher = Journey.get().dispatcher().editor();
    registerEvent(FoundSolutionEvent.class, event -> {
      Journey.logger().debug("FoundSolutionEvent");
      sessionItineraries.put(event.getSession().uuid(), event.getItinerary());
    });
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
