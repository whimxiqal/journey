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

package net.whimxiqal.journey.integration.citizens.config;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.whimxiqal.journey.integration.citizens.JourneyCitizens;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;

public class ConfigSettings {

  public static final ConfigSetting<Integer> NPC_NAVIGATOR_MAX_NPCS = new NonNullConfigSetting<>("npc-navigator.max-npcs", 128, Integer.class);

  public static final ConfigSetting<EntityType> NPC_NAVIGATOR_DEFAULT_OPTIONS_ENTITY_TYPE = new ConfigSetting<>(
      "npc-navigator.default-options.entity-type",
      EntityType.FOX,
      (config, thisSetting) -> {
        String stringValue = config.getString(thisSetting.path());
        if (stringValue == null) {
          return thisSetting.def();
        }
        EntityType val = Registry.ENTITY_TYPE.get(NamespacedKey.minecraft(stringValue));
        if (val == null) {
          logInvalid(thisSetting, stringValue, thisSetting.def().getKey().getKey());
          return thisSetting.def();
        }
        return val;
      },
      EntityType.class);

  public static final ConfigSetting<String> NPC_NAVIGATOR_DEFAULT_OPTIONS_NAME = new NonNullConfigSetting<>(
      "npc-navigator.default-options.name",
      "Guide",
      String.class);
  public static final ConfigSetting<Particle> SPAWN_PARTICLE = new ConfigSetting<>(
      "npc-navigator.spawn-particle",
      Particle.REDSTONE,
      (config, thisSetting) -> {
        String stringValue = config.getString(thisSetting.path());
        if (stringValue == null) {
          return null;
        }
        try {
          return Particle.valueOf(stringValue.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
          logInvalid(thisSetting, stringValue, thisSetting.def().toString().toLowerCase(Locale.ENGLISH));
          return thisSetting.def();
        }
      },
      Particle.class);
  private static final Pattern HEX_VALUE_PATTERN = Pattern.compile("^[0-9a-fA-F]{6}$");
  public static final ConfigSetting<Color> SPAWN_PARTICLE_COLOR = new ConfigSetting<>(
      "npc-navigator.spawn-particle-color",
      Color.WHITE,
      (config, thisSetting) -> {
        String stringValue = JourneyCitizens.get().getConfig().getString(thisSetting.path());
        if (stringValue == null) {
          return thisSetting.def();
        }
        Matcher matcher = HEX_VALUE_PATTERN.matcher(stringValue);
        if (!matcher.matches()) {
          logInvalid(thisSetting, stringValue, "ffffff (white)");
          return thisSetting.def();
        }
        String rawHex = matcher.group(0);
        int red = Integer.valueOf(rawHex.substring(0, 2), 16);
        int green = Integer.valueOf(rawHex.substring(2, 4), 16);
        int blue = Integer.valueOf(rawHex.substring(4, 6), 16);
        return Color.fromRGB(red, green, blue);
      },
      Color.class);

  private ConfigSettings() {
  }

  private static <T> void logInvalid(ConfigSetting<T> setting, String stringValue, String stringDefault) {
    JourneyCitizens.logger().warning("[config.yml] Setting " + setting.path()
        + " has invalid value: " + stringValue
        + ". Using default: " + stringDefault);
  }

  public static void testLoadAll() {
    Arrays.stream(ConfigSettings.class.getDeclaredFields())
        .filter(field -> ConfigSetting.class.isAssignableFrom(field.getType()))
        .filter(field -> Modifier.isStatic(field.getModifiers()))
        .forEach(field -> {
          try {
            ((ConfigSetting<?>) field.get(null)).load();
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        });
  }
}
