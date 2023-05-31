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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.data.TestDataManager;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.platform.TestJourneyPlayer;
import net.whimxiqal.journey.platform.TestPlatformProxy;
import net.whimxiqal.journey.platform.WorldLoader;
import net.whimxiqal.journey.util.CommonLogger;
import org.junit.jupiter.api.BeforeAll;

public class JourneyTestHarness {

  public static final UUID PLAYER_UUID = UUID.randomUUID();
  protected static final boolean DEBUG = false;
  protected static final Map<UUID, Itinerary> SESSION_ITINERARIES = new HashMap<>();
  private static boolean initialized = false;

  @BeforeAll
  static void initializeHarness() throws NoSuchFieldException, IllegalAccessException {
    if (initialized) {
      return;
    }
    initialized = true;

    Settings.MAX_SEARCHES.setValue(10);

    TestProxy proxy = new TestProxy();
    Journey.get().registerProxy(proxy);
    proxy.schedulingManager.startMainThread();

    // set test data manager
    Field workManagerField = Journey.class.getDeclaredField("dataManager");
    workManagerField.setAccessible(true);
    workManagerField.set(Journey.get(), new TestDataManager());

    if (DEBUG) {
      Journey.logger().setLevel(CommonLogger.LogLevel.DEBUG);
    }
    Journey.get().init();
    WorldLoader.initWorlds();

    TestPlatformProxy.onlinePlayers.add(new TestJourneyPlayer(PLAYER_UUID));
    Journey.get().tunnelManager().register(player -> TestPlatformProxy.tunnels);
  }

  protected final <T> T runOnMainThread(Supplier<T> supplier) throws ExecutionException, InterruptedException {
    CompletableFuture<T> future = new CompletableFuture<>();
    Journey.get().proxy().schedulingManager().schedule(() -> future.complete(supplier.get()), false);
    return future.get();
  }

}
