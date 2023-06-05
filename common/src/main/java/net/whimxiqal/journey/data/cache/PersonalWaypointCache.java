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

package net.whimxiqal.journey.data.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.data.DataAccessException;
import net.whimxiqal.journey.data.PersonalWaypointProvider;
import net.whimxiqal.journey.data.Waypoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PersonalWaypointCache implements PersonalWaypointProvider {

  /**
   * Time until a request for info causes an update
   */
  private static final long DATA_SOFT_LIFETIME_MS = 1000 * 60;  // 1 minute

  /**
   * Time until cached data is purged
   */
  private static final long DATA_HARD_LIFETIME_MS = 1000 * 60 * 60;  // 1 hour
  private static final int PURGE_PERIOD_TICKS = 20 * 60; // 1 minute
  private final Map<UUID, PersonalWaypointInformation> information = new ConcurrentHashMap<>();
  private UUID purgeTaskId = null;

  public void initialize() {
    purgeTaskId = Journey.get().proxy().schedulingManager().scheduleRepeat(() -> {
      List<UUID> toRemove = new LinkedList<>();
      for (Map.Entry<UUID, PersonalWaypointInformation> entry : information.entrySet()) {
        if (entry.getValue().timestamp + DATA_HARD_LIFETIME_MS < System.currentTimeMillis()) {
          toRemove.add(entry.getKey());
        }
      }
      for (UUID playerUuid : toRemove) {
        information.remove(playerUuid);
      }
    }, false, PURGE_PERIOD_TICKS);
  }

  public void shutdown() {
    if (purgeTaskId != null) {
      Journey.get().proxy().schedulingManager().cancelTask(purgeTaskId);
      purgeTaskId = null;
    }
  }

  /**
   * Updates the cache for the given player. If force is false, then the cached
   * data will only be updated if enough time has passed and the data is deemed stale.
   *
   * @param playerUuid the player's uuid
   * @param force      whether to force an update, even if the lifetime of the data has not expired
   * @return a future, to be completed once the update is complete
   */
  public Future<Void> update(UUID playerUuid, boolean force) {
    PersonalWaypointInformation info = information.get(playerUuid);
    if (info == null) {
      info = new PersonalWaypointInformation(Collections.emptyList());
      info.refreshing.set(true);
      information.put(playerUuid, info);  // just put in blank info
      return sendInfoRequest(playerUuid);
    }
    if (info.refreshing.get()) {
      return CompletableFuture.completedFuture(null);  // already in progress, do nothing
    }
    if (info.timestamp + DATA_SOFT_LIFETIME_MS < System.currentTimeMillis() || force) {
      boolean setRefreshing = info.refreshing.compareAndSet(false, true);
      // only send request if we actually are the ones to set the refreshing flag
      if (setRefreshing) {
        return sendInfoRequest(playerUuid);
      }
    }
    return CompletableFuture.completedFuture(null);  // no update needed
  }

  private Future<Void> sendInfoRequest(UUID playerUuid) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    Journey.get().proxy().schedulingManager().schedule(() -> {
      // Request on async thread
      Collection<Waypoint> waypoints = Journey.get().proxy().dataManager().personalWaypointManager().getAll(playerUuid, false);
      information.put(playerUuid, new PersonalWaypointInformation(waypoints));
      future.complete(null);
    }, true);
    return future;
  }

  @Override
  public @Nullable String getName(@NotNull UUID playerUuid, @NotNull Cell cell) throws DataAccessException {
    throw new UnsupportedOperationException("This operation isn't implemented");
  }

  @Override
  public @Nullable Cell getWaypoint(@NotNull UUID playerUuid, @NotNull String name) throws DataAccessException {
    throw new UnsupportedOperationException("This operation isn't implemented");
  }

  @Override
  public boolean isPublic(@NotNull UUID playerUuid, @NotNull String name) throws DataAccessException {
    throw new UnsupportedOperationException("This operation isn't implemented");
  }

  @Override
  public List<Waypoint> getAll(@NotNull UUID playerUuid, boolean justPublic) throws DataAccessException {
    update(playerUuid, false);
    return information.get(playerUuid).waypoints
        .stream()
        .filter(waypoint -> !justPublic || waypoint.publicity())
        .collect(Collectors.toList());
  }

  @Override
  public int getCount(UUID playerUuid, boolean justPublic) {
    update(playerUuid, false);
    return information.get(playerUuid).waypoints
        .stream()
        .filter(waypoint -> !justPublic || waypoint.publicity())
        .mapToInt(waypoint -> 1)
        .sum();
  }

  private static class PersonalWaypointInformation {
    final Collection<Waypoint> waypoints;
    final double timestamp = System.currentTimeMillis();
    final AtomicBoolean refreshing = new AtomicBoolean(false);

    PersonalWaypointInformation(Collection<Waypoint> waypoints) {
      this.waypoints = waypoints;
    }

  }

}
