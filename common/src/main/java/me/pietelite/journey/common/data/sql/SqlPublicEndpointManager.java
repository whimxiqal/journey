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

import me.pietelite.journey.common.data.DataAccessException;
import me.pietelite.journey.common.data.PublicEndpointManager;
import me.pietelite.journey.common.navigation.Cell;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A public endpoint manager implemented for SQL.
 */
public abstract class SqlPublicEndpointManager
    extends SqlEndpointManager
    implements PublicEndpointManager {

  /**
   * General constructor.
   *
   * @param connectionController the connection controller
   */
  public SqlPublicEndpointManager(SqlConnectionController connectionController) {
    super(connectionController);
  }

  @Override
  public void addPublicEndpoint(@NotNull Cell cell, @NotNull String name)
      throws IllegalArgumentException, DataAccessException {
    addEndpoint(null, cell, name);
  }

  @Override
  public void removePublicEndpoint(@NotNull Cell cell) throws DataAccessException {
    removeEndpoint(null, cell);
  }

  @Override
  public void removePublicEndpoint(@NotNull String name) throws DataAccessException {
    removeEndpoint(null, name);
  }

  @Override
  public @Nullable String getPublicEndpointName(@NotNull Cell cell) throws DataAccessException {
    return getEndpointName(null, cell);
  }

  @Override
  public @Nullable Cell getPublicEndpoint(@NotNull String name) throws DataAccessException {
    return getEndpoint(null, name);
  }

  @Override
  public Map<String, Cell> getPublicEndpoints() throws DataAccessException {
    return getEndpoints(null);
  }
}
