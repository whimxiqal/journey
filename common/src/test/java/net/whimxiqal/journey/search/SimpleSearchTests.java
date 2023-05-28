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

package net.whimxiqal.journey.search;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.JourneyTestHarness;
import net.whimxiqal.journey.manager.DistributedWorkManager;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.platform.TestPlatformProxy;
import net.whimxiqal.journey.platform.WorldLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleSearchTests extends JourneyTestHarness {

  private UUID lastSessionUuid;

  private SearchSession destinationSession(String origin, String destination) {
    return new PlayerDestinationGoalSearchSession(PLAYER_UUID,
        TestPlatformProxy.pois.get(origin),
        TestPlatformProxy.pois.get(destination),
        true);
  }

  private SearchSession domainSession(String origin, int domainDestination) {
    return new PlayerDomainGoalSearchSession(PLAYER_UUID, TestPlatformProxy.pois.get(origin), domainDestination);
  }

  private void runDestinationSearch(String origin, String destination, ResultState expectedResult) throws InterruptedException {
    runSearch(destinationSession(origin, destination), expectedResult);
  }

  private void runDomainSearch(String origin, String domainName, ResultState expectedResult) throws InterruptedException {
    runSearch(domainSession(origin, Journey.get().domainManager().domainIndex(WorldLoader.getWorld(domainName).uuid)), expectedResult);
  }

  private void runSearch(SearchSession session, ResultState expectedResult) throws InterruptedException {
    session.initialize();
    lastSessionUuid = session.uuid();
    Assertions.assertEquals(ResultState.IDLE, session.getState());
    CompletableFuture<ResultState> future = session.search(20);
    ResultState state = future.join();
    while (!Journey.get().dispatcher().isEmpty()) {
      Thread.sleep(100);
      Journey.logger().debug("Waiting 100 ms for event dispatcher to dispatch all events");
    }
    Assertions.assertEquals(expectedResult, state, "Expected state of session search was incorrect");
    if (expectedResult.isSuccessful()) {
      Assertions.assertTrue(SESSION_ITINERARIES.containsKey(session.uuid()));
    }
  }

  @Test
  void destinationSearchSameWorld() throws InterruptedException {
    runDestinationSearch("1", "2", ResultState.STOPPED_SUCCESSFUL);
  }

  @Test
  void destinationSearchCaching() throws InterruptedException {
    runDestinationSearch("1", "2", ResultState.STOPPED_SUCCESSFUL);
    Thread.sleep(100);  // wait for caching to complete on async thread
    runDestinationSearch("1", "2", ResultState.STOPPED_SUCCESSFUL);  // should use cached values
  }

  @Test
  void destinationSearchesMultipleWorlds() throws InterruptedException {
    runDestinationSearch("1", "3", ResultState.STOPPED_SUCCESSFUL);
  }

  @Test
  void destinationSearchesPathLengths() throws InterruptedException {
    runDestinationSearch("1", "3", ResultState.STOPPED_SUCCESSFUL);
    Itinerary oneToThree = SESSION_ITINERARIES.get(lastSessionUuid);

    runDestinationSearch("2", "3", ResultState.STOPPED_SUCCESSFUL);
    Itinerary twoToThree = SESSION_ITINERARIES.get(lastSessionUuid);

    // The distance from 1 -> a/A -> b/B -> 3 is shorter than the distance from 2 -> c/C -> b/B -> 3
    Assertions.assertTrue(oneToThree.cost() < twoToThree.cost());
  }

  @Test
  void destinationSearchesFailed() throws InterruptedException {
    runDestinationSearch("3", "1", ResultState.STOPPED_FAILED);
    runDestinationSearch("1", "4", ResultState.STOPPED_FAILED);
  }

  @Test
  void domainSearchSuccess() throws InterruptedException {
    runDomainSearch("1", "world2", ResultState.STOPPED_SUCCESSFUL);
    runDomainSearch("2", "world2", ResultState.STOPPED_SUCCESSFUL);
  }

  @Test
  void domainSearchFail() throws InterruptedException {
    // 3 is completed walled off
    runDomainSearch("3", "world2", ResultState.STOPPED_FAILED);
  }

  @Test
  void domainSearchSameWorldError() throws InterruptedException {
    // cannot go to the same world
    runDomainSearch("1", "world1", ResultState.STOPPED_ERROR);
    runDomainSearch("2", "world1", ResultState.STOPPED_ERROR);
    runDomainSearch("3", "world1", ResultState.STOPPED_ERROR);
  }

  @Test
  void stressTest() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
    // Set the work manager manually in main Journey class so we can test with multiple threads and active path searches
    Field workManagerField = Journey.class.getDeclaredField("workManager");
    workManagerField.setAccessible(true);
    workManagerField.set(Journey.get(), new DistributedWorkManager(5, 10));

    // Set cells-per-execution-cycle to something small to stress-test the DistributedWorkManager
    AbstractPathTrial.CELLS_PER_EXECUTION_CYCLE = 5;
    final int runs = 100;
    final int total = runs * 12;
    AtomicInteger finished = new AtomicInteger(0);

    BiFunction<String, String, SearchSession> newSearch = (origin, destination) ->
        new PlayerDestinationGoalSearchSession(UUID.randomUUID(),
        TestPlatformProxy.pois.get(origin),
        TestPlatformProxy.pois.get(destination),
        false);

    BiConsumer<SearchSession, ResultState> runSearchAsync = (session, expected) -> {
      session.initialize();
      Assertions.assertEquals(ResultState.IDLE, session.getState());
      session.search(2).thenAccept(result -> {
        finished.incrementAndGet();
        Assertions.assertEquals(expected, result, "Failed search: " + session);
      });
    };

    for (int i = 0; i < runs; i++) {
      runSearchAsync.accept(newSearch.apply("1", "2"), ResultState.STOPPED_SUCCESSFUL);
      runSearchAsync.accept(newSearch.apply("1", "3"), ResultState.STOPPED_SUCCESSFUL);
      runSearchAsync.accept(newSearch.apply("1", "4"), ResultState.STOPPED_FAILED);
      runSearchAsync.accept(newSearch.apply("2", "3"), ResultState.STOPPED_SUCCESSFUL);
      runSearchAsync.accept(newSearch.apply("2", "4"), ResultState.STOPPED_FAILED);
      runSearchAsync.accept(newSearch.apply("3", "4"), ResultState.STOPPED_FAILED);

      runSearchAsync.accept(newSearch.apply("2", "1"), ResultState.STOPPED_SUCCESSFUL);
      runSearchAsync.accept(newSearch.apply("3", "1"), ResultState.STOPPED_SUCCESSFUL);
      runSearchAsync.accept(newSearch.apply("4", "1"), ResultState.STOPPED_FAILED);
      runSearchAsync.accept(newSearch.apply("3", "2"), ResultState.STOPPED_SUCCESSFUL);
      runSearchAsync.accept(newSearch.apply("4", "2"), ResultState.STOPPED_FAILED);
      runSearchAsync.accept(newSearch.apply("4", "3"), ResultState.STOPPED_FAILED);
    }

    long failureTime = System.currentTimeMillis() + (1000 * 5);  // 5 seconds until we consider it failure
    while (finished.get() != total) {
      Thread.sleep(100);
      if (System.currentTimeMillis() > failureTime) {
        Assertions.fail("The test took too long. Only " + finished.get() + " out of " + total + " finished.");
      }
    }
  }

}
