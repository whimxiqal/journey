/*
 * Copyright 2021 Pieter Svenson
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

package edu.whimc.journey.common;

import edu.whimc.journey.common.cache.PathCache;
import edu.whimc.journey.common.config.ConfigManager;
import edu.whimc.journey.common.navigation.Cell;
import edu.whimc.journey.common.search.event.SearchDispatcher;
import edu.whimc.journey.common.util.LoggerCommon;
import lombok.Getter;
import lombok.Setter;

public final class JourneyCommon {

  private JourneyCommon() {
  }

  private static SearchDispatcher<?, ?, ?> searchEventDispatcher;

  @Getter @Setter
  private static ConfigManager configManager;

  private static LoggerCommon logger;

  /**
   * A cache of all previously calculated paths.
   */
  private static PathCache<?, ?> pathCache;

  @SuppressWarnings("unchecked")
  public static <T extends Cell<T, D>, D> SearchDispatcher<T, D, ?> getSearchEventDispatcher() {
    if (searchEventDispatcher == null) {
      throw new IllegalStateException("No search dispatcher! Did you forget to initialize it?");
    }
    return (SearchDispatcher<T, D, ?>) searchEventDispatcher;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Cell<T, D>, D, E> void setSearchEventDispatcher(SearchDispatcher<T, D, E> dispatcher) {
    JourneyCommon.searchEventDispatcher = dispatcher;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Cell<T, D>, D> PathCache<T, D> getPathCache() {
    if (pathCache == null) {
      throw new IllegalStateException("No path cache! Did you forget to initialize it?");
    }
    return (PathCache<T, D>) pathCache;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Cell<T, D>, D> void setPathCache(PathCache<T, D> pathCache) {
    JourneyCommon.pathCache = pathCache;
  }

  public static void setLogger(LoggerCommon logger) {
    JourneyCommon.logger = logger;
  }

  public static LoggerCommon getLogger() {
    return logger;
  }

}
