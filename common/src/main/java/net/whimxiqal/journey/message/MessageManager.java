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
    Journey.logger().info("language locale: " + languageLocale.toLanguageTag());
    ResourceBundle bundle;
    boolean found = true;
    try {
      bundle = ResourceBundle.getBundle("messages", languageLocale);
      Journey.logger().info("bundle locale: " + bundle.getLocale().toLanguageTag());
    } catch (MissingResourceException e) {
      found = false;
      bundle = ResourceBundle.getBundle("messages", Settings.EXTRA_LANGUAGE.getDefaultValue());
      Journey.logger().info("default bundle locale: " + bundle.getLocale().toLanguageTag());
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
