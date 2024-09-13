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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.config.struct.ConfigFillPhase;
import net.whimxiqal.journey.config.struct.ConfigItemType;
import net.whimxiqal.journey.config.struct.ConfigItemsRule;
import net.whimxiqal.journey.config.struct.ConfigStaticButton;
import net.whimxiqal.journey.data.StorageMethod;
import net.whimxiqal.journey.navigation.TrailNavigator;
import net.whimxiqal.journey.Color;

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
      = new StringListSetting("navigation.trail.particle", List.of("glow", "dust"), true);

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

  public static final Setting<Integer> GUI_ROWS
      = new IntegerSetting("gui.rows", 6, true, 3, 6);

  public static final Setting<Boolean> GUI_PLAY_SOUND
      = new BooleanSetting("gui.play-sound", true, true);

  public static final Setting<List<ConfigFillPhase>> GUI_FILL = new ListSetting<>("gui.fill",
      Collections.singletonList(new ConfigFillPhase(ConfigItemType.of("black_stained_glass_pane"), false, false, true, true)),
      ConfigFillPhase.class,
      true);

  public static final Setting<ConfigStaticButton> GUI_HOME_BUTTON = new Setting<>("gui.buttons.home",
      new ConfigStaticButton(1, 1,
          new ConfigItemType(null,
              Collections.emptyList(),
              "player_head",
              Collections.emptyMap(),
              "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGYxN2E2YTlhZmFhN2IwN2U0MjE4ZmU1NTVmMjgyM2IwMjg0Y2Q2OWI0OWI2OWI5N2ZhZTY3ZWIyOTc2M2IifX19")),
      ConfigStaticButton.class, true);

  public static final Setting<ConfigStaticButton> GUI_BACK_BUTTON = new Setting<>("gui.buttons.back",
      new ConfigStaticButton(1, 2,
          new ConfigItemType(null,
              Collections.emptyList(),
              "dark_oak_boat",
              Collections.emptyMap(),
              "")),
      ConfigStaticButton.class, true);

  public static final Setting<ConfigStaticButton> GUI_OPEN_FLAG_EDITOR_BUTTON = new Setting<>("gui.buttons.flag-editor-open",
      new ConfigStaticButton(1, 9,
          new ConfigItemType(null,
              Collections.emptyList(),
              "player_head",
              Collections.emptyMap(),
              "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y2NmY3ZjAzMTI1Y2Y1NDczMzY5NmYzNjMyZjBkOWU2NDcwYmFhYjg0OTg0N2VhNWVhMmQ3OTE1NmFkMGY0MCJ9fX0=")),
      ConfigStaticButton.class, true);

  public static final Setting<ConfigStaticButton> GUI_CLOSE_FLAG_EDITOR_BUTTON = new Setting<>("gui.buttons.flag-editor-close",
      new ConfigStaticButton(1, 9,
          new ConfigItemType(null,
              Collections.emptyList(),
              "player_head",
              Collections.emptyMap(),
              "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFhOTEyZTMzMmZjMDAxMGJlYmQwZjkzYTE0ZDhlM2VhNjVkMTMwMTEwMGNlYTNmYzVhZTcxOTkwZDk4NTgwNyJ9fX0=")),
      ConfigStaticButton.class, true);

  public static final Setting<ConfigStaticButton> GUI_PREVIOUS_PAGE = new Setting<>("gui.buttons.page-previous",
      new ConfigStaticButton(6, 2,
          new ConfigItemType(null,
              Collections.emptyList(),
              "paper",
              Collections.emptyMap(),
              "")),
      ConfigStaticButton.class, true);

  public static final Setting<ConfigStaticButton> GUI_NEXT_PAGE = new Setting<>("gui.buttons.page-next",
      new ConfigStaticButton(6, 8,
          new ConfigItemType(null,
              Collections.emptyList(),
              "paper",
              Collections.emptyMap(),
              "")),
      ConfigStaticButton.class, true);

  public static final Setting<List<ConfigItemsRule>> GUI_CONTENT_SCOPES_RULE_LIST = new ListSetting<>("gui.content.scopes",
      Collections.singletonList(new ConfigItemsRule(".*", List.of(
          ConfigItemType.of("blue_stained_glass"),
          ConfigItemType.of("brown_stained_glass"),
          ConfigItemType.of("cyan_stained_glass"),
          ConfigItemType.of("gray_stained_glass"),
          ConfigItemType.of("green_stained_glass"),
          ConfigItemType.of("light_blue_stained_glass"),
          ConfigItemType.of("light_gray_stained_glass"),
          ConfigItemType.of("lime_stained_glass"),
          ConfigItemType.of("magenta_stained_glass"),
          ConfigItemType.of("orange_stained_glass"),
          ConfigItemType.of("pink_stained_glass"),
          ConfigItemType.of("purple_stained_glass"),
          ConfigItemType.of("red_stained_glass"),
          ConfigItemType.of("white_stained_glass"),
          ConfigItemType.of("yellow_stained_glass")
      ))),
      ConfigItemsRule.class,
      true);

  public static final Setting<List<ConfigItemsRule>> GUI_CONTENT_DESTINATIONS_RULE_LIST = new ListSetting<>("gui.content.destinations",
      Collections.singletonList(new ConfigItemsRule(".*", List.of(
          ConfigItemType.of("black_terracotta"),
          ConfigItemType.of("blue_terracotta"),
          ConfigItemType.of("brown_terracotta"),
          ConfigItemType.of("cyan_terracotta"),
          ConfigItemType.of("gray_terracotta"),
          ConfigItemType.of("green_terracotta"),
          ConfigItemType.of("light_blue_terracotta"),
          ConfigItemType.of("light_gray_terracotta"),
          ConfigItemType.of("lime_terracotta"),
          ConfigItemType.of("magenta_terracotta"),
          ConfigItemType.of("orange_terracotta"),
          ConfigItemType.of("pink_terracotta"),
          ConfigItemType.of("purple_terracotta"),
          ConfigItemType.of("red_terracotta"),
          ConfigItemType.of("white_terracotta"),
          ConfigItemType.of("yellow_terracotta")
      ))),
      ConfigItemsRule.class,
      true);

  public static final Setting<List<ConfigItemsRule>> GUI_CONTENT_FLAGS_RULE_LIST = new ListSetting<>("gui.content.flags",
      Collections.singletonList(new ConfigItemsRule(".*", List.of(
          ConfigItemType.of("stone_button"),
          ConfigItemType.of("polished_blackstone_button"),
          ConfigItemType.of("oak_button"),
          ConfigItemType.of("spruce_button"),
          ConfigItemType.of("birch_button"),
          ConfigItemType.of("jungle_button"),
          ConfigItemType.of("acacia_button"),
          ConfigItemType.of("dark_oak_button"),
          ConfigItemType.of("crimson_button"),
          ConfigItemType.of("warped_button")
      ))),
      ConfigItemsRule.class,
      true);

  public static final Setting<Locale> EXTRA_LANGUAGE = new LocaleSetting("extra.language", Locale.ENGLISH, false);

  public static final Setting<Boolean> EXTRA_CHECK_LATEST_VERSION_ON_STARTUP = new BooleanSetting("extra.check-latest-version-on-startup",
      true,
      false);

  public static final Setting<Boolean> EXTRA_FIND_INTEGRATIONS_ON_STARTUP = new BooleanSetting("extra.find-integrations-on-startup",
      true,
      false);

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

      if (field.getAnnotation(DeprecatedSetting.class) != null) {
        setting.deprecated = true;
      }
    }
  }

  private Settings() {
  }

}
