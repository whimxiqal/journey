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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.whimxiqal.journey.Journey;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class LocaleSetting extends Setting<Locale> {

  public final static Set<Locale> ALLOWED_LOCALES = new HashSet<>();

  static {
    ALLOWED_LOCALES.add(Locale.ENGLISH);
    ALLOWED_LOCALES.add(Locale.GERMAN);
    ALLOWED_LOCALES.add(Locale.SIMPLIFIED_CHINESE);
    ALLOWED_LOCALES.add(Locale.TRADITIONAL_CHINESE);
    ALLOWED_LOCALES.add(Locale.forLanguageTag("tr"));
  }

  LocaleSetting(@NotNull String path, @NotNull Locale defaultValue, boolean reloadable) {
    super(path, defaultValue, Locale.class, reloadable);
  }

  @Override
  public boolean valid(Locale value) {
    return LocaleSetting.ALLOWED_LOCALES.contains(value);
  }

  @Override
  public @NotNull String printValue(Locale value) {
    return value.toLanguageTag();
  }

  @Override
  public Locale deserialize(CommentedConfigurationNode node) throws SerializationException {
    String string = node.getString();
    if (string == null) {
      return defaultValue;
    }
    Locale locale;
    if (string.isEmpty()) {
      locale = Locale.getDefault();
      if (!ALLOWED_LOCALES.contains(locale)) {
        Journey.logger().error("Tried using default locale for determining language, but default locale " + locale.toLanguageTag() + " is not supported. Using " + defaultValue.toLanguageTag());
        return defaultValue;
      }
    } else {
      locale = Locale.forLanguageTag(string);
      if (!ALLOWED_LOCALES.contains(locale)) {
        Journey.logger().error("Locale " + locale.toLanguageTag() + " was specified in config.yml, but it is not supported. Using " + defaultValue.toLanguageTag());
        return defaultValue;
      }
    }
    return locale;
  }
}
