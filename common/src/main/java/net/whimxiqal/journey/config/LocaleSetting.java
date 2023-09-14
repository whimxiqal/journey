package net.whimxiqal.journey.config;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.whimxiqal.journey.message.MessageManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class LocaleSetting extends Setting<Locale> {

  public final static Set<Locale> ALLOWED_LOCALES = new HashSet<>();

  static {
    ALLOWED_LOCALES.add(Locale.ENGLISH);
    ALLOWED_LOCALES.add(Locale.GERMAN);
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
    return Locale.forLanguageTag(string);
  }
}
