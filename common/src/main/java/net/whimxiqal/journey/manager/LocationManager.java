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

package net.whimxiqal.journey.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.chunk.BlockProvider;
import net.whimxiqal.journey.navigation.journey.PlayerJourneySession;

public class LocationManager {
  public static final long VISITATION_TIMEOUT_MS = 10;  // Any visits with 10 ms
  // Known player locations, updated lazily and used for updating the journey sessions
  private final Map<UUID, Cell> locations = new HashMap<>();
  // If this contains a player uuid, then consider them at the world's surface. The boolean value is whether this is outdated info or not.
  private final Map<UUID, AtSurfaceInfo> atSurface = new HashMap<>();
  private long lastVisitTime = 0;

  /**
   * Attempt to update the cache with given player's location. The update will not go through
   * if the player's location has not changed since the last update.
   *
   * @param playerUuid the player's uuid
   * @param location   the player's location
   * @return true if the update succeeded, false if no change
   * @throws ExecutionException   see {@link BlockProvider#isAtSurface}
   * @throws InterruptedException see {@link BlockProvider#isAtSurface}
   */
  public boolean tryUpdateLocation(UUID playerUuid, Cell location) throws ExecutionException, InterruptedException {
    Cell currentCachedLocation = locations.get(playerUuid);
    if (currentCachedLocation != null && currentCachedLocation.equals(location)) {
      return false;
    }

    locations.put(playerUuid, location);
    AtSurfaceInfo atSurfaceInfo = atSurface.get(playerUuid);
    if (atSurfaceInfo == null) {
      atSurface.put(playerUuid, new AtSurfaceInfo(BlockProvider.isAtSurface(Journey.get().proxy().platform(), location)));
    } else {
      atSurfaceInfo.stale = true;
    }
    return true;
  }

  /**
   * Attempt to update the cache with whether the given player is at the surface of the world.
   *
   * @param playerUuid the player's uuid
   * @param location   the location
   * @return true if the player is at surface
   * @throws ExecutionException   see {@link BlockProvider#isAtSurface}
   * @throws InterruptedException see {@link BlockProvider#isAtSurface}
   */
  public boolean getAndTryUpdateIsAtSurface(UUID playerUuid, Cell location) throws ExecutionException, InterruptedException {
    AtSurfaceInfo atSurfaceInfo = atSurface.get(playerUuid);
    if (atSurfaceInfo == null || atSurfaceInfo.stale) {
      boolean ret = BlockProvider.isAtSurface(Journey.get().proxy().platform(), location);
      atSurface.put(playerUuid, new AtSurfaceInfo(ret));
      return ret;
    }
    return atSurfaceInfo.atSurface;
  }

  public void handlePlayerMoveEvent(UUID playerUuid, Cell location) {
    long now = System.currentTimeMillis();
    if (now < lastVisitTime + VISITATION_TIMEOUT_MS) {
      // ignore movements if they're too frequent
      return;
    }
    lastVisitTime = now;
    boolean updatedLocation;
    try {
      updatedLocation = tryUpdateLocation(playerUuid, location);
    } catch (ExecutionException | InterruptedException e) {
      Journey.logger().error("Internal error trying to update players cached location on move event: " + playerUuid);
      e.printStackTrace();
      return;
    }
    if (updatedLocation) {
      PlayerJourneySession playerJourney = Journey.get().searchManager().getJourney(playerUuid);
      if (playerJourney != null) {
        playerJourney.visit(location);
      }
    }
  }

  private static class AtSurfaceInfo {
    final boolean atSurface;
    boolean stale;

    AtSurfaceInfo(boolean atSurface) {
      this.atSurface = atSurface;
      this.stale = false;
    }
  }

}
