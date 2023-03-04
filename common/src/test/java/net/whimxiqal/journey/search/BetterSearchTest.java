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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.JourneyTestHarness;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.platform.TestPlatformProxy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BetterSearchTest extends JourneyTestHarness {

  private UUID lastSessionUuid;

  private SearchSession destinationSession(String origin, String destination) {
    return new PlayerDestinationGoalSearchSession(UUID.randomUUID(),
        TestPlatformProxy.pois.get(origin),
        TestPlatformProxy.pois.get(destination),
        true);
  }

  private SearchSession domainSession(String origin, int domainDestination) {
    return new PlayerDomainGoalSearchSession(UUID.randomUUID(), TestPlatformProxy.pois.get(origin), domainDestination);
  }

  private void runDestinationSearch(String origin, String destination, ResultState expectedResult) throws InterruptedException {
    runSearch(destinationSession(origin, destination), expectedResult);
  }

  private void runDomainSearch(String origin, String domainId, ResultState expectedResult) throws InterruptedException {
    runSearch(domainSession(origin, Journey.get().domainManager().domainIndex(domainId)), expectedResult);
  }

  private void runSearch(SearchSession session, ResultState expectedResult) throws InterruptedException {
    session.initialize();
    lastSessionUuid = session.uuid();
    Assertions.assertEquals(ResultState.IDLE, session.getState());
    CompletableFuture<ResultState> future = session.search(20);
    ResultState state = future.join();
    while (!Journey.get().dispatcher().isEmpty()) {
      Thread.sleep(100);
      Journey.logger().debug("Waiting for event dispatcher to dispatch all events");
    }
    Assertions.assertEquals(expectedResult, state, "Expected state of session search was incorrect");
    if (expectedResult.isSuccessful()) {
      Assertions.assertTrue(sessionItineraries.containsKey(session.uuid()));
    }
  }

  @Test
  void destinationSearches() throws InterruptedException {
    runDestinationSearch("1", "2", ResultState.STOPPED_SUCCESSFUL);
    runDestinationSearch("1", "3", ResultState.STOPPED_SUCCESSFUL);
    Itinerary oneToThree = sessionItineraries.get(lastSessionUuid);
    runDestinationSearch("3", "1", ResultState.STOPPED_FAILED);
    runDestinationSearch("1", "4", ResultState.STOPPED_FAILED);
    runDestinationSearch("2", "3", ResultState.STOPPED_SUCCESSFUL);
    Itinerary twoToThree = sessionItineraries.get(lastSessionUuid);

    Assertions.assertTrue(oneToThree.cost() < twoToThree.cost());
  }

  @Test
  void domainSearches() throws InterruptedException {
    runDomainSearch("1", "world1", ResultState.STOPPED_ERROR);  // cannot go to the same world
    runDomainSearch("2", "world1", ResultState.STOPPED_ERROR);  // cannot go to the same world
    runDomainSearch("3", "world1", ResultState.STOPPED_ERROR);  // cannot go to the same world
    runDomainSearch("1", "world2", ResultState.STOPPED_SUCCESSFUL);
    runDomainSearch("2", "world2", ResultState.STOPPED_SUCCESSFUL);
    runDomainSearch("3", "world2", ResultState.STOPPED_FAILED);
  }

}
