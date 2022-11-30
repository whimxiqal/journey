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

package me.pietelite.journey.common.search;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.pietelite.journey.common.JourneyTestHarness;
import me.pietelite.journey.common.search.flag.FlagSet;
import me.pietelite.journey.platform.TestPlatformProxy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BetterSearchTest extends JourneyTestHarness {

  private UUID lastSessionUuid;

  private SearchSession session(String origin, String destination) {
    return new PlayerDestinationGoalSearchSession(UUID.randomUUID(),
        TestPlatformProxy.pois.get(origin),
        TestPlatformProxy.pois.get(destination),
        new FlagSet(),
        true);
  }

  private void runSearch(String origin, String destination, ResultState expectedResult) {
    SearchSession session = session(origin, destination);
    lastSessionUuid = session.uuid();
    Assertions.assertEquals(ResultState.IDLE, session.getState());
    CompletableFuture<ResultState> future = session.search(20);
    ResultState state = future.join();
    Assertions.assertEquals(expectedResult, state, "Expected state of session search was incorrect");
//    if (expectedResult.isSuccessful()) {
//      Assertions.assertTrue(sessionItineraries.containsKey(session.uuid()));
//    }
  }

  @Test
  void searches() {
    runSearch("1", "2", ResultState.STOPPED_SUCCESSFUL);
    runSearch("1", "3", ResultState.STOPPED_SUCCESSFUL);
//    Itinerary oneToThree = sessionItineraries.get(lastSessionUuid);
    runSearch("3", "1", ResultState.STOPPED_FAILED);
    runSearch("1", "4", ResultState.STOPPED_FAILED);
    runSearch("2", "3", ResultState.STOPPED_SUCCESSFUL);
//    Itinerary twoToThree = sessionItineraries.get(lastSessionUuid);

//    Assertions.assertTrue(oneToThree.cost() < twoToThree.cost());
  }

}
