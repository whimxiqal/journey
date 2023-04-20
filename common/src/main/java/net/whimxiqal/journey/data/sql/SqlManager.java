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

package net.whimxiqal.journey.data.sql;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * A general SQL manager for storage purposes.
 */
public abstract class SqlManager {

  public static final String WAYPOINTS_TABLE = "journey_waypoints";
  public static final String CACHED_PATHS_TABLE = "journey_cached_paths";
  public static final String CACHED_PATH_CELLS_TABLE = "journey_cached_path_cells";
  public static final String CACHED_PATH_MODES_TABLE = "journey_cached_path_modes";
  public static final String TUNNELS_TABLE = "journey_tunnels";

  private final SqlConnectionController connectionController;

  /**
   * General constructor.
   *
   * @param connectionController a connection controller
   */
  public SqlManager(SqlConnectionController connectionController) {
    this.connectionController = connectionController;
  }

  public SqlConnectionController getConnectionController() {
    return connectionController;
  }

}
