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

package net.whimxiqal.journey.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.data.StorageMethod;

/**
 * An enumeration of all {@link Setting}s. No need to register anywhere, that's done dynamically.
 */
public final class Settings {

  public static final Setting<Integer> DEFAULT_SEARCH_TIMEOUT
      = new IntegerSetting("search.flag.default-timeout", 30, 0, 24 * 60 * 60 /* a day */);

  public static final Setting<Boolean> DEFAULT_FLY_FLAG
      = new BooleanSetting("search.flag.default-fly", true);

  public static final Setting<Boolean> DEFAULT_DOORS_FLAG
      = new BooleanSetting("search.flag.default-door", true);

  public static final Setting<Boolean> DEFAULT_DIG_FLAG
      = new BooleanSetting("search.flag.default-dig", false);

  public static final Setting<Integer> MAX_PATH_BLOCK_COUNT
      = new IntegerSetting("search.max-path-block-count", 100000, 10, 10000000);

  public static final Setting<Boolean> ALLOW_CHUNK_GENERATION
      = new BooleanSetting("search.chunk-gen.allow", false);

  public static final Setting<Integer> MAX_SEARCHES
      = new IntegerSetting("search.max-searches", 16, 0, 1000000);

  public static final Setting<Long> MAX_CACHED_CELLS
      = new LongSetting("storage.cache.max_cells", 500000, 1, Long.MAX_VALUE) /* Default is somewhere around 10-20 MB */;

  public static final Setting<String> STORAGE_ADDRESS
      = new StringSetting("storage.auth.address", "my.address");

  public static final Setting<String> STORAGE_DATABASE
      = new StringSetting("storage.auth.database", "my_database");

  public static final Setting<String> STORAGE_USERNAME
      = new StringSetting("storage.auth.username", "username");

  public static final Setting<String> STORAGE_PASSWORD
      = new StringSetting("storage.auth.password", "p@ssword");

  public static final Setting<StorageMethod> STORAGE_TYPE
      = new EnumSetting<>("storage.type", StorageMethod.SQLITE, StorageMethod.class);

  public static final Map<String, Setting<?>> ALL_SETTINGS = new HashMap<>();

  static {
    Arrays.stream(Settings.class.getDeclaredFields())
        .map(field -> {
          try {
            return field.get(null);
          } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
          }
        })
        .filter(object -> object instanceof Setting)
        .map(object -> ((Setting<?>) object))
        .forEach(setting -> ALL_SETTINGS.put(setting.getPath(), setting));
  }

  public static void validate() {
    ALL_SETTINGS.forEach((path, setting) -> {
      if (!setting.isValid()) {
        Journey.logger().warn("Setting " + setting.getPath() + " has invalid value " + setting.printValue() + ". Using default: " + setting.getDefaultValue() + ".");
        setting.setToDefault();
      }
    });
  }

  private Settings() {
  }

}
