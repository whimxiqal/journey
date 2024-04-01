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
