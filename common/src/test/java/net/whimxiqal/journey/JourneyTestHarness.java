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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.platform.TestJourneyPlayer;
import net.whimxiqal.journey.platform.TestPlatformProxy;
import net.whimxiqal.journey.platform.WorldLoader;
import net.whimxiqal.journey.util.CommonLogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

public class JourneyTestHarness {

  public static final UUID PLAYER_UUID = UUID.randomUUID();
  public static final boolean DEBUG = false;
  protected static final Map<UUID, Itinerary> SESSION_ITINERARIES = new HashMap<>();

  @BeforeAll
  static void initializeHarness() {
    Settings.MAX_SEARCHES.setValue(10);

    Journey.create();
    TestProxy proxy = new TestProxy(new TestPlatformProxy());
    Journey.get().registerProxy(proxy);

    if (DEBUG) {
      Journey.logger().setLevel(CommonLogger.LogLevel.DEBUG);
    }
    if (!Journey.get().init()) {
      Assertions.fail("Journey initialization failed");
    }
    WorldLoader.initWorlds();

    TestPlatformProxy.onlinePlayers.add(new TestJourneyPlayer(PLAYER_UUID));
    Journey.get().tunnelManager().register(player -> TestPlatformProxy.tunnels);
  }

  @AfterAll
  static void shutdown() {
    Journey.get().shutdown();
    Journey.remove();
  }

  protected final <T> T runOnMainThread(Supplier<T> supplier) throws ExecutionException, InterruptedException {
    CompletableFuture<T> future = new CompletableFuture<>();
    Journey.get().proxy().schedulingManager().schedule(() -> future.complete(supplier.get()), false);
    return future.get();
  }

}
