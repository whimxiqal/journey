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

package edu.whimc.indicator.config;

import edu.whimc.indicator.Indicator;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigManager {

  FileConfiguration configuration = Indicator.getInstance().getConfig();

  public ConfigManager() {
    loadSettings();
  }

  /**
   * The same as doing <code>Workshop.getInstance.getConfig</code>.
   *
   * @return the configuration
   */
  public FileConfiguration get() {
    return configuration;
  }

  private Map<String, Setting> getSettings() {
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
        .map(object -> ((Setting) object))
        .collect(Collectors.toMap(Setting::getPath, setting -> setting));
  }

  public void dumpSettings() {
    getSettings().forEach((key, value) -> configuration.set(key, value.getValue()));
  }

  @NotNull
  public String dumpState() {
    dumpSettings();
    return configuration.saveToString();
  }

  public void loadSettings() {
    getSettings().forEach((key, value) -> {
      if (configuration.contains(key)) {
        value.setValue(configuration.get(key));
      } else {
        configuration.set(key, value.getValue());
      }
    });
  }

  public void loadState(String state) throws Exception {
    configuration.loadFromString(state);
    loadSettings();
  }

  @NotNull
  public String getFileName() {
    return "config.yml";
  }
}
