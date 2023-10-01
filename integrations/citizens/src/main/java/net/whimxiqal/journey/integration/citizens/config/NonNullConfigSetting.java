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

import net.whimxiqal.journey.integration.citizens.JourneyCitizens;

public class NonNullConfigSetting<T> extends ConfigSetting<T> {
  public NonNullConfigSetting(String path, T def, ConfigSettingLoader<T> loader, Class<T> clazz) {
    super(path, def, loader, clazz);
  }

  public NonNullConfigSetting(String path, T def, Class<T> clazz) {
    super(path, def, clazz);
  }

  @Override
  public T load() {
    T loaded = super.load();
    if (loaded == null) {
      JourneyCitizens.logger().warning("[config.yml] A value for setting " + path() + " was not specified. Using default: " + def().toString());
      return def();
    }
    return loaded;
  }
}
