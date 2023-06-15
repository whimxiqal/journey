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

import java.util.LinkedList;
import java.util.List;
import net.whimxiqal.journey.Cell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestPublicWaypointManager implements PublicWaypointManager {

  private final List<Waypoint> waypoints = new LinkedList<>();

  @Override
  public void add(@NotNull Cell cell, @NotNull String name) throws IllegalArgumentException, DataAccessException {
    waypoints.add(new Waypoint(name, cell, true));
  }

  @Override
  public void remove(@NotNull Cell cell) throws DataAccessException {
    waypoints.removeIf(waypoint -> waypoint.location().equals(cell));
  }

  @Override
  public void remove(@NotNull String name) throws DataAccessException {
    waypoints.removeIf(waypoint -> waypoint.name().equals(name));
  }

  @Override
  public void renameWaypoint(String name, String newName) throws DataAccessException {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nullable String getName(@NotNull Cell cell) throws DataAccessException {
    return waypoints.stream().filter(waypoint -> waypoint.location().equals(cell)).map(Waypoint::name).findFirst().orElse(null);
  }

  @Override
  public @Nullable Cell getWaypoint(@NotNull String name) throws DataAccessException {
    return waypoints.stream().filter(waypoint -> waypoint.name().equals(name)).map(Waypoint::location).findFirst().orElse(null);
  }

  @Override
  public List<Waypoint> getAll() throws DataAccessException {
    return waypoints;
  }

  @Override
  public int getCount() {
    return waypoints.size();
  }
}
