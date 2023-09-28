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

package net.whimxiqal.journey.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.whimxiqal.journey.Cell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestPersonalWaypointManager implements PersonalWaypointManager {

  private final Map<UUID, List<Waypoint>> waypoints = new HashMap<>();

  @Override
  public void add(@NotNull UUID playerUuid, @NotNull Cell cell, @NotNull String name) throws IllegalArgumentException, DataAccessException {
    waypoints.computeIfAbsent(playerUuid, k -> new LinkedList<>()).add(new Waypoint(name, cell, true));
  }

  @Override
  public void remove(@NotNull UUID playerUuid, @NotNull Cell cell) throws DataAccessException {
    List<Waypoint> waypoints = this.waypoints.get(playerUuid);
    if (waypoints == null) {
      return;
    }
    waypoints.removeIf(waypoint -> waypoint.location().equals(cell));
  }

  @Override
  public void setPublic(@NotNull UUID playerUuid, @NotNull String name, boolean isPublic) throws DataAccessException {
    // ignore
  }

  @Override
  public void remove(@NotNull UUID playerUuid, @NotNull String name) throws DataAccessException {
    List<Waypoint> waypoints = this.waypoints.get(playerUuid);
    if (waypoints == null) {
      return;
    }
    waypoints.removeIf(waypoint -> waypoint.name().equals(name));
  }

  @Override
  public void renameWaypoint(UUID uuid, String name, String newName) throws DataAccessException {
    // ignore
  }

  @Override
  public @Nullable Cell getWaypoint(@NotNull UUID playerUuid, @NotNull String name) throws DataAccessException {
    List<Waypoint> waypoints = this.waypoints.get(playerUuid);
    if (waypoints == null) {
      return null;
    }
    return waypoints.stream().filter(waypoint -> waypoint.name().equals(name)).findFirst().map(Waypoint::location).orElse(null);
  }

  @Override
  public boolean isPublic(@NotNull UUID playerUuid, @NotNull String name) throws DataAccessException {
    return true;
  }

  @Override
  public List<Waypoint> getAll(@NotNull UUID playerUuid, boolean justPublic) throws DataAccessException {
    List<Waypoint> ret = waypoints.get(playerUuid);
    if (ret == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(ret);
  }

  @Override
  public int getCount(UUID playerUuid, boolean justPublic) {
    return getAll(playerUuid, justPublic).size();
  }
}
