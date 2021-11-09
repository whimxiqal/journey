/*
 * MIT License
 *
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
 *
 */

package edu.whimc.journey.common;

import edu.whimc.journey.common.cache.PathCache;
import edu.whimc.journey.common.config.ConfigManager;
import edu.whimc.journey.common.ml.NeuralNetwork;
import edu.whimc.journey.common.navigation.Cell;
import edu.whimc.journey.common.search.event.SearchDispatcher;
import edu.whimc.journey.common.util.LoggerCommon;
import edu.whimc.journey.common.util.MinecraftConversions;
import lombok.Getter;
import lombok.Setter;

/**
 * The central utility class to provide methods for all platform
 * implementations of the Journey plugin.
 */
public final class JourneyCommon {

  private static SearchDispatcher<?, ?, ?> searchEventDispatcher;
  @Getter
  @Setter
  private static ConfigManager configManager;
  private static LoggerCommon logger;
  /**
   * A cache of all previously calculated paths.
   */
  private static PathCache<?, ?> pathCache;

  private static MinecraftConversions<?, ?> conversions;

  @Getter
  @Setter
  private static NeuralNetwork neuralNetwork;

  private JourneyCommon() {
  }

  /**
   * Get the event dispatcher used in a {@link edu.whimc.journey.common.search.SearchSession}.
   * It is up to the caller of this method to use the same generics used when
   * instantiating the event dispatcher at start-up.
   *
   * @param <T> the location type
   * @param <D> the domain type
   * @return the dispatcher
   */
  @SuppressWarnings("unchecked")
  public static <T extends Cell<T, D>, D> SearchDispatcher<T, D, ?> getSearchEventDispatcher() {
    if (searchEventDispatcher == null) {
      throw new IllegalStateException("No search dispatcher! Did you forget to initialize it?");
    }
    return (SearchDispatcher<T, D, ?>) searchEventDispatcher;
  }

  /**
   * Set the search dispatcher used in a {@link edu.whimc.journey.common.search.SearchSession}.
   *
   * @param dispatcher the dispatcher
   * @param <T>        the location type
   * @param <D>        the domain type
   * @param <E>        the event type ultimately used to catch the events.
   */
  public static <T extends Cell<T, D>, D, E> void setSearchEventDispatcher(
      SearchDispatcher<T, D, E> dispatcher) {
    JourneyCommon.searchEventDispatcher = dispatcher;
  }

  /**
   * Get the cache of all memoized paths.
   *
   * @param <T> the location type
   * @param <D> the domain type
   * @return the cache
   */
  @SuppressWarnings("unchecked")
  public static <T extends Cell<T, D>, D> PathCache<T, D> getPathCache() {
    if (pathCache == null) {
      throw new IllegalStateException("No path cache! Did you forget to initialize it?");
    }
    return (PathCache<T, D>) pathCache;
  }

  /**
   * Set the path cache.
   *
   * @param pathCache the path cache
   * @param <T>       the location type
   * @param <D>       the domain type
   */
  public static <T extends Cell<T, D>, D> void setPathCache(PathCache<T, D> pathCache) {
    JourneyCommon.pathCache = pathCache;
  }

  /**
   * Get a simple logger, which can be used anywhere in common files.
   *
   * @return the logger
   */
  public static LoggerCommon getLogger() {
    return logger;
  }

  /**
   * Set the simple logger, implemented using the implementation platform's logger.
   *
   * @param logger the logger
   */
  public static void setLogger(LoggerCommon logger) {
    JourneyCommon.logger = logger;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Cell<T, D>, D> MinecraftConversions<T, D> getConversions() {
    if (conversions == null) {
      throw new IllegalStateException("No Minecraft conversions! Did you forget to initialize it?");
    }
    return (MinecraftConversions<T, D>) conversions;
  }

  /**
   * Set the path cache.
   *
   * @param conversions the path cache
   * @param <T>         the location type
   * @param <D>         the domain type
   */
  public static <T extends Cell<T, D>, D> void setConversions(MinecraftConversions<T, D> conversions) {
    JourneyCommon.conversions = conversions;
  }


}
