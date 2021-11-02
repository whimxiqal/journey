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
 *
 */

package edu.whimc.journey.common.cache;

import com.google.common.collect.Maps;
import edu.whimc.journey.common.navigation.Cell;
import edu.whimc.journey.common.navigation.ModeTypeGroup;
import edu.whimc.journey.common.navigation.Path;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The java-serializable cache that stores the saved {@link Path}s for retrieval
 * after a server restart.
 *
 * @param <T> the location type
 * @param <D> the domain type
 * @see edu.whimc.journey.common.search.SearchSession
 */
public class PathCache<T extends Cell<T, D>, D> implements Serializable {

  public static final String SERIALIZED_PATH_CACHE_FILENAME = "paths.ser";

  private final Map<T, Map<T, Map<ModeTypeGroup, CachedPath<T, D>>>> cache = new ConcurrentHashMap<>();
  private int size = 0;

  /**
   * Add a path into the cache.
   *
   * @param origin      the origin of the path
   * @param destination the destination of the path
   * @param modeTypes   the mode types that were used to create this path
   * @param path        the path itself
   * @return the replaced path, if there was one
   */
  @Nullable
  public CachedPath<T, D> put(@NotNull T origin,
                              @NotNull T destination,
                              @NotNull ModeTypeGroup modeTypes,
                              @NotNull CachedPath<T, D> path) {
    cache.putIfAbsent(origin, Maps.newHashMap());
    Map<T, Map<ModeTypeGroup, CachedPath<T, D>>> destinationMap = cache.get(origin);
    destinationMap.putIfAbsent(destination, Maps.newHashMap());
    Map<ModeTypeGroup, CachedPath<T, D>> modeTypeMap = destinationMap.get(destination);

    CachedPath<T, D> replaced = modeTypeMap.get(modeTypes);
    modeTypeMap.put(modeTypes, path);



    if (replaced == null) {
      System.out.println("Cached path, " + origin + " to " + destination + ": " + (path.get() == null ? "empty" : "present"));
      size++;
    } else {
      System.out.println("Replaced cached path, " + origin + " to " + destination + ": " + (path.get() == null ? "empty" : "present"));
    }
    return replaced;
  }

  /**
   * Get a path from the cache.
   *
   * @param origin      the origin
   * @param destination the destination
   * @param modeTypes   the mode types used to get there
   * @return the path
   */
  @Nullable
  public Path<T, D> get(T origin, T destination, ModeTypeGroup modeTypes) {
    Map<ModeTypeGroup, CachedPath<T, D>> modeTypeMap = getModeTypeMap(origin, destination);
    if (modeTypeMap != null) {
      Path<T, D> path = modeTypeMap.get(modeTypes).get();
      System.out.println("Retrieving path, " + origin + " to " + destination + ": " + (path == null ? "empty" : "present"));
      return path;
    }
    return null;
  }

  @Nullable
  public Path<T, D> remove(T origin, T destination, ModeTypeGroup modeTypes) {
    Map<ModeTypeGroup, CachedPath<T, D>> modeTypeMap = getModeTypeMap(origin, destination);
    if (modeTypeMap != null) {
      CachedPath<T, D> cachedPath = modeTypeMap.remove(modeTypes);
      if (cachedPath == null) {
        return null;
      } else {
        return cachedPath.get();
      }
    }
    return null;
  }

  /**
   * Get the map of mode types to paths, given an origin and a destination.
   * This is useful because sometimes, we know where we want to start and end in the path,
   * but we don't know which mode types we need to have enabled for a given search caller
   * to get a valid cached path.
   *
   * @param origin      the origin
   * @param destination the destination
   * @return the map of mode types to paths
   */
  @Nullable
  public Map<ModeTypeGroup, CachedPath<T, D>> getModeTypeMap(T origin, T destination) {
    if (cache.containsKey(origin)) {
      Map<T, Map<ModeTypeGroup, CachedPath<T, D>>> destinationMap = cache.get(origin);
      if (destinationMap.containsKey(destination)) {
        return destinationMap.get(destination);
      }
    }
    return null;
  }

  /**
   * Get the size of the path cache.
   *
   * @return the size
   */
  public int size() {
    return size;
  }

  /**
   * Clear the entire cache.
   */
  public void clear() {
    cache.clear();
    size = 0;
  }

  /**
   * Return whether this cached path exists.
   *
   * @param origin      the origin
   * @param destination the destination
   * @param modeTypes   the mode types
   * @return true if it is contained
   */
  public boolean contains(T origin, T destination, ModeTypeGroup modeTypes) {
    Map<ModeTypeGroup, CachedPath<T, D>> modeTypeMap = getModeTypeMap(origin, destination);
    if (modeTypeMap == null) {
      return false;
    }
    return modeTypeMap.containsKey(modeTypes);
  }

  public static class CachedPath<T extends Cell<T, D>, D> implements Serializable {
    private Path<T, D> path;

    public static <T extends Cell<T, D>, D> CachedPath<T, D> of(@NotNull Path<T, D> path) {
      return new CachedPath<>(path);
    }

    public static <T extends Cell<T, D>, D> CachedPath<T, D> empty() {
      return new CachedPath<>(null);
    }

    private CachedPath(@Nullable Path<T, D> path) {
      this.path = path;
    }

    public boolean isEmpty() {
      return path == null;
    }

    public boolean isPresent() {
      return path != null;
    }

    public void ifPresent(Consumer<Path<T, D>> pathConsumer) {
      if (path != null) {
        pathConsumer.accept(path);
      }
    }

    @Nullable
    public Path<T, D> get() {
      return path;
    }

    @NotNull
    public Path<T, D> require() {
      if (path == null) {
        throw new NoSuchElementException("This cached path is empty");
      }
      return path;
    }
  }

}
