/*
 * MIT License
 *
 * Copyright (c) 2020 Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package edu.whimc.indicator.common.config;

import edu.whimc.indicator.Indicator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.configuration.InvalidConfigurationException;

public class ConfigManager {

  private final String fileName;

  public ConfigManager(String fileName) {
    this.fileName = fileName;
    load();
    save();
  }

  private Map<String, Setting<?>> getSettings() {
    return Arrays.stream(Settings.class.getDeclaredFields())
        .map(field -> {
          try {
            return field.get(this);
          } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
          }
        })
        .filter(object -> object instanceof Setting)
        .map(object -> ((Setting<?>) object))
        .collect(Collectors.toMap(Setting::getPath, setting -> setting));
  }

  public void dumpSettings() {
    getSettings().forEach((path, setting) -> Indicator.getInstance().getConfig().set(path, setting.printValue()));
  }

  public void save() {
    File configFile = new File(Indicator.getInstance().getDataFolder(), this.fileName);
    try {
      if (configFile.createNewFile()) {
        Indicator.getInstance().getLogger().info("Created config file for Indicator");
      }
      try (FileOutputStream fos = new FileOutputStream(configFile)) {
        // Stage static setting values into config object
        dumpSettings();
        // Write config to file
        fos.write(Indicator.getInstance().getConfig().saveToString().getBytes(StandardCharsets.UTF_8));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void loadSettings() {
    getSettings().forEach((s, setting) -> {
      if (Indicator.getInstance().getConfig().contains(s)) {
        parseAndSetValue(setting, s);
      } else {
        Indicator.getInstance().getConfig().set(s, setting.getValue());
      }
    });
  }

  /**
   * Helper function to ensure proper typing
   *
   * @param setting     the setting
   * @param stringValue the string version of the setting value
   * @param <X>         the type of setting
   */
  private <X> void parseAndSetValue(Setting<X> setting, String stringValue) {
    setting.setValue(setting.parseValue(Objects.requireNonNull(Indicator.getInstance().getConfig().getString(stringValue))));
  }

  public void load() {
    File configFile = new File(Indicator.getInstance().getDataFolder(), this.fileName);
    try {
      if (configFile.createNewFile()) {
        Indicator.getInstance().getLogger().info("Created config file for Indicator");
      }
      try (FileInputStream fis = new FileInputStream(configFile)) {
        byte[] data = new byte[(int) configFile.length()];
        if (fis.read(data) < 0) {
          Indicator.getInstance().getLogger().severe("Configuration file could not be read");
        }
        // Stage into config object
        Indicator.getInstance().getConfig().loadFromString(new String(data, StandardCharsets.UTF_8));
        // Put config object into static settings
        loadSettings();
      } catch (InvalidConfigurationException e) {
        Indicator.getInstance().getLogger().severe("Your configuration file is malformed!");
        e.printStackTrace();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
