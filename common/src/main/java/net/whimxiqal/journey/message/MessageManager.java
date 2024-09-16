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

package net.whimxiqal.journey.message;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.config.Settings;

public class MessageManager {

  private static final MiniMessage miniMessage = MiniMessage.builder()
      .tags(TagResolver.builder()
          .resolver(StandardTags.color())
          .resolver(StandardTags.decorations())
          .resolver(TagResolver.resolver("theme", Tag.styling(style -> style.color(Formatter.THEME))))
          .resolver(TagResolver.resolver("prefix", Tag.selfClosingInserting(Formatter.prefix())))
          .build())
      .build();
  private final Map<String, String> messages = new ConcurrentHashMap<>();

  public static MiniMessage miniMessage() {
    return miniMessage;
  }

  public void initialize() {
    final Locale languageLocale = Settings.EXTRA_LANGUAGE.getValue();
    ResourceBundle bundle;
    boolean found = true;
    try {
      bundle = ResourceBundle.getBundle("messages", languageLocale);
    } catch (MissingResourceException e) {
      found = false;
      bundle = ResourceBundle.getBundle("messages", Settings.EXTRA_LANGUAGE.getDefaultValue());
    }
    for (Enumeration<String> keys = bundle.getKeys(); keys.hasMoreElements(); ) {
      String key = keys.nextElement();
      messages.put(key, bundle.getString(key));
    }
    if (!found) {
      Journey.logger().error("Could not find any language file for locale found in the config file (" + languageLocale
          + "). Using default: " + Settings.EXTRA_LANGUAGE.getDefaultValue().toLanguageTag());
    }
  }

  public String getMessage(String key) {
    return messages.get(key);
  }
}
