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

package me.pietelite.journey.common.data.sql;

import java.util.Map;
import java.util.UUID;
import me.pietelite.journey.common.data.DataAccessException;
import me.pietelite.journey.common.data.PersonalWaypointManager;
import me.pietelite.journey.common.navigation.Cell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A combination of an endpoint manager for SQL and a personal endpoint manager.
 */
public abstract class SqlPersonalWaypointManager
    extends SqlWaypointManager
    implements PersonalWaypointManager {

  /**
   * Default constructor.
   *
   * @param connectionController the connection controller
   */
  public SqlPersonalWaypointManager(SqlConnectionController connectionController) {
    super(connectionController);
  }

  @Override
  public void add(@NotNull UUID playerUuid, @NotNull Cell cell, @NotNull String name)
      throws IllegalArgumentException, DataAccessException {
    this.addEndpoint(playerUuid, cell, name);
  }

  @Override
  public void remove(@NotNull UUID playerUuid, @NotNull Cell cell)
      throws DataAccessException {
    this.removeEndpoint(playerUuid, cell);
  }

  @Override
  public void remove(@NotNull UUID playerUuid, @NotNull String name)
      throws DataAccessException {
    this.removeEndpoint(playerUuid, name);
  }

  @Override
  public @Nullable String getName(@NotNull UUID playerUuid, @NotNull Cell cell)
      throws DataAccessException {
    return this.getEndpointName(playerUuid, cell);
  }

  @Override
  public @Nullable Cell getWaypoint(@NotNull UUID playerUuid, @NotNull String name)
      throws DataAccessException {
    return this.getEndpoint(playerUuid, name);
  }

  @Override
  public Map<String, Cell> getAll(@NotNull UUID playerUuid)
      throws DataAccessException {
    return this.getEndpoints(playerUuid);
  }
}
