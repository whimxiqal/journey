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

package me.pietelite.journey.common.data;

import java.util.Map;
import java.util.UUID;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.search.SearchSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A general description of how to handle storage for personal search endpoints.
 *
 * @see SearchSession
 */
public interface PersonalWaypointManager {

  /**
   * Add a player to a specific cell to the database with a unique name.
   *
   * @param playerUuid the player's uuid
   * @param cell       the cell to add
   * @param name       the name of the location
   * @throws IllegalArgumentException if the player and cell are not a unique combination
   *                                  or if the player and name are not a unique combination
   */
  void add(@NotNull UUID playerUuid,
           @NotNull Cell cell,
           @NotNull String name) throws IllegalArgumentException, DataAccessException;

  /**
   * Remove a player and a cell from the database. Name is irrelevant.
   * Does nothing if this combination does not exist.
   *
   * @param playerUuid the player's uuid
   * @param cell       the cell location
   */
  void remove(@NotNull UUID playerUuid,
              @NotNull Cell cell) throws DataAccessException;

  void setPublic(@NotNull UUID playerUuid,
                 @NotNull String name,
                 boolean isPublic) throws DataAccessException;

  /**
   * Remove a player and a named cell from the database. Cell location is irrelevant.
   * Does nothing if this combination does not exist.
   *
   * @param playerUuid the player's uuid
   * @param name       the name of the location
   */
  void remove(@NotNull UUID playerUuid,
              @NotNull String name) throws DataAccessException;

  /**
   * Check if a player has a personal endpoint at the certain cell location.
   *
   * @param playerUuid the player's uuid
   * @param cell       the cell location
   * @return true if the cell exists for the given player
   */
  default boolean hasWaypoint(@NotNull UUID playerUuid,
                              @NotNull Cell cell) throws DataAccessException {
    return getName(playerUuid, cell) != null;
  }

  /**
   * Check if a player has a certain named personal endpoint.
   *
   * @param playerUuid the player's uuid
   * @param name       the cell name
   * @return true if the cell exists for the given player
   */
  default boolean hasWaypoint(@NotNull UUID playerUuid,
                              @NotNull String name) throws DataAccessException {
    return getWaypoint(playerUuid, name) != null;
  }

  /**
   * Rename the waypoint.
   *
   * @param uuid    the player's uuid
   * @param name    the waypoint name
   * @param newName the new waypoint name
   */
  void renameWaypoint(UUID uuid, String name, String newName) throws DataAccessException;

  /**
   * Get the name of a personal location with a given unique player and cell.
   *
   * @param playerUuid the player's uuid
   * @param cell       the personal location
   * @return the cell's name, or null if it doesn't exist
   */
  @Nullable
  String getName(@NotNull UUID playerUuid,
                 @NotNull Cell cell) throws DataAccessException;

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
   * Get a list of all personal endpoints for a player.
   *
   * @param playerUuid the player's uuid
   * @return all names of cells mapped to their corresponding cells
   */
  Map<String, Cell> getAll(@NotNull UUID playerUuid) throws DataAccessException;
}
