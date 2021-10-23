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

package edu.whimc.indicator.common;

import edu.whimc.indicator.common.cache.PathCache;
import edu.whimc.indicator.common.config.ConfigManager;
import edu.whimc.indicator.common.navigation.Cell;
import edu.whimc.indicator.common.search.event.SearchDispatcher;
import lombok.Getter;
import lombok.Setter;

public final class IndicatorCommon {

  private IndicatorCommon() {
  }

  // TODO get rid of all mention of Spigot in common folder
  // TODO create a logger here

  private static SearchDispatcher<?, ?, ?> searchEventDispatcher;

  @Getter @Setter
  private static ConfigManager configManager;

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
    IndicatorCommon.searchEventDispatcher = dispatcher;
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
    IndicatorCommon.pathCache = pathCache;
  }

}
