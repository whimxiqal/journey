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

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.data.StorageMethod;
import net.whimxiqal.journey.navigation.TrailNavigator;
import net.whimxiqal.journey.navigation.option.Color;

/**
 * An enumeration of all {@link Setting}s. No need to register anywhere, that's done dynamically.
 */
public final class Settings {

  public static final Setting<Integer> DEFAULT_SEARCH_TIMEOUT
      = new IntegerSetting("search.flag.default-timeout", 30, true, 0, 24 * 60 * 60 /* a day */);

  public static final Setting<Boolean> DEFAULT_FLY_FLAG
      = new BooleanSetting("search.flag.default-fly", true, true);

  public static final Setting<Boolean> DEFAULT_DOORS_FLAG
      = new BooleanSetting("search.flag.default-door", true, true);

  public static final Setting<Boolean> DEFAULT_DIG_FLAG
      = new BooleanSetting("search.flag.default-dig", false, true);

  public static final Setting<String> DEFAULT_NAVIGATOR
      = new StringSetting("search.flag.default-navigator", TrailNavigator.TRAIL_NAVIGATOR_ID, true);

  public static final Setting<Component> DEFAULT_NAVIGATION_COMPLETION_MESSAGE
      = new ComponentSetting("navigation.completion.message", Component.empty(), true);

  public static final Setting<Component> DEFAULT_NAVIGATION_COMPLETION_TITLE
      = new ComponentSetting("navigation.completion.title", Component.empty(), true);

  public static final Setting<Component> DEFAULT_NAVIGATION_COMPLETION_SUBTITLE
      = new ComponentSetting("navigation.completion.subtitle", Component.empty(), true);

  public static final Setting<List<String>> DEFAULT_TRAIL_PARTICLE
      = new StringListSetting("navigation.trail.particle", List.of("glow", "redstone"), true);

  public static final Setting<List<Color>> DEFAULT_TRAIL_COLOR
      = new ColorListSetting("navigation.trail.color", List.of(new Color(172, 21, 219)), true);

  public static final Setting<Double> DEFAULT_TRAIL_WIDTH
      = new DoubleSetting("navigation.trail.width", 1.0, true, 0.1, 5);

  public static final Setting<Double> DEFAULT_TRAIL_DENSITY
      = new DoubleSetting("navigation.trail.density", 5.0, true, 1.0, 10.0);

  public static final Setting<Integer> MAX_PATH_BLOCK_COUNT
      = new IntegerSetting("search.max-path-block-count", 100000, true, 1000, 10000000);

  public static final Setting<Boolean> ALLOW_CHUNK_GENERATION
      = new BooleanSetting("search.chunk-gen.allow", false, false);

  public static final Setting<Integer> MAX_SEARCHES
      = new IntegerSetting("search.max-searches", 16, false, 0, Integer.MAX_VALUE);

  public static final Setting<Integer> MAX_CACHED_CELLS
      = new IntegerSetting("storage.cache.max-cells", 500000, true, 1, Integer.MAX_VALUE) /* Default is somewhere around 10-20 MB */;

  public static final Setting<String> STORAGE_ADDRESS
      = new StringSetting("storage.auth.address", "my.address", false);

  public static final Setting<String> STORAGE_DATABASE
      = new StringSetting("storage.auth.database", "my_database", false);

  public static final Setting<String> STORAGE_USERNAME
      = new StringSetting("storage.auth.username", "username", false);

  public static final Setting<String> STORAGE_PASSWORD
      = new StringSetting("storage.auth.password", "p@ssword", false);

  public static final Setting<StorageMethod> STORAGE_TYPE
      = new EnumSetting<>("storage.type", StorageMethod.SQLITE, StorageMethod.class, false);

  public static final Setting<Locale> LOCALE = new LocaleSetting("language", Locale.ENGLISH, false);

  public static final Map<String, Setting<?>> ALL_SETTINGS = new LinkedHashMap<>();  // preserve order

  static {
    for (Field field : Settings.class.getDeclaredFields()) {
      Object obj;
      try {
        obj = field.get(null);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
        continue;
      }
      if (!(obj instanceof Setting<?> setting)) {
        continue;
      }
      Setting<?> previous = ALL_SETTINGS.put(setting.getPath(), setting);
      if (previous != null) {
        throw new IllegalStateException("Found two settings with the same path: " + setting.getPath());
      }
    }
  }

  private Settings() {
  }

}
