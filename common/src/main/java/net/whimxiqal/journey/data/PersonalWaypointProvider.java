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

import java.util.List;
import java.util.UUID;
import net.whimxiqal.journey.Cell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PersonalWaypointProvider {

  /**
   * Get a specific cell with a given unique player and name combination.
   *
   * @param playerUuid the player's uuid
   * @param name       the cell name
   * @return the cell, or null if it doesn't
   */
  @Nullable
  Cell getWaypoint(@NotNull UUID playerUuid,
                   @NotNull String name) throws DataAccessException;

  boolean isPublic(@NotNull UUID playerUuid,
                   @NotNull String name) throws DataAccessException;

  /**
   * Get an unmodifiable list of all personal endpoints for a player.
   *
   * @param playerUuid the player's uuid
   * @param justPublic whether to show only public waypoints
   * @return all names of cells mapped to their corresponding cells
   */
  List<Waypoint> getAll(@NotNull UUID playerUuid, boolean justPublic) throws DataAccessException;

  /**
   * Get the number of entries found with this player uuid and the public value
   *
   * @param playerUuid the player's uuid
   * @param justPublic whether to show only public waypoints
   * @return the number of entries
   */
  int getCount(UUID playerUuid, boolean justPublic);
}
