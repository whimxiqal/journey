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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.data.DataAccessException;
import net.whimxiqal.journey.data.PublicWaypointProvider;
import net.whimxiqal.journey.data.Waypoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PublicWaypointCache implements PublicWaypointProvider {

  /**
   * Time until a request for info causes an update
   */
  private static final long DATA_SOFT_LIFETIME_MS = 1000 * 60;  // 1 minute

  private final AtomicReference<PublicWaypointInformation> information = new AtomicReference<>(new PublicWaypointInformation(Collections.emptyList()));

  public void initialize() {
    sendInfoRequest();
  }

  /**
   * Updates the cache. If force is false, then the cached
   * data will only be updated if enough time has passed and the data is deemed stale.
   *
   * @param force whether to force an update, even if the lifetime of the data has not expired
   * @return a future, to be completed once the update is complete
   */
  public Future<Void> update(boolean force) {
    PublicWaypointInformation info = information.get();
    if (info.refreshing.get()) {
      return CompletableFuture.completedFuture(null);  // already in progress, do nothing
    }
    if (info.timestamp + DATA_SOFT_LIFETIME_MS < System.currentTimeMillis() || force) {
      boolean setRefreshing = info.refreshing.compareAndSet(false, true);
      // only send request if we actually are the ones to set the refreshing flag
      if (setRefreshing) {
        return sendInfoRequest();
      }
    }
    return CompletableFuture.completedFuture(null);  // no update needed
  }

  private Future<Void> sendInfoRequest() {
    CompletableFuture<Void> future = new CompletableFuture<>();
    Journey.get().proxy().schedulingManager().schedule(() -> {
      // Request on async thread
      information.set(new PublicWaypointInformation(Journey.get().proxy().dataManager().publicWaypointManager().getAll()));
      future.complete(null);
    }, true);
    return future;
  }

  @Override
  public @Nullable String getName(@NotNull Cell cell) throws DataAccessException {
    throw new UnsupportedOperationException("This operation isn't implemented");
  }

  @Override
  public @Nullable Cell getWaypoint(@NotNull String name) throws DataAccessException {
    throw new UnsupportedOperationException("This operation isn't implemented");
  }

  @Override
  public List<Waypoint> getAll() throws DataAccessException {
    update(false);
    return information.get().waypoints;
  }

  @Override
  public int getCount() {
    update(false);
    return information.get().waypoints.size();
  }

  private static class PublicWaypointInformation {
    final List<Waypoint> waypoints;
    final double timestamp = System.currentTimeMillis();
    final AtomicBoolean refreshing = new AtomicBoolean(false);

    PublicWaypointInformation(List<Waypoint> waypoints) {
      this.waypoints = waypoints;
    }

  }
}
