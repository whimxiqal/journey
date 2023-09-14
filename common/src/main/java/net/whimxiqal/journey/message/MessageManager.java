package net.whimxiqal.journey.message;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.config.Settings;

public class MessageManager {

  private final static Pattern REPLACEABLE_ELEMENT_REGEX = Pattern.compile("\\{}");
  private final Map<String, String> messages = new ConcurrentHashMap<>();

  public void initialize() {
    final Locale stringLocal = Settings.LOCALE.getValue();
    ResourceBundle bundle;
    boolean found = true;
    try {
      bundle = ResourceBundle.getBundle("messages", stringLocal);
    } catch (MissingResourceException e) {
      found = false;
      bundle = ResourceBundle.getBundle("messages", Settings.LOCALE.getDefaultValue());
    }
    for (Enumeration<String> keys = bundle.getKeys(); keys.hasMoreElements();) {
      String key = keys.nextElement();
      messages.put(key, bundle.getString(key));
    }
    if (!found) {
      Journey.logger().error("Could not find any language file for locale found in the config file (" + stringLocal
          + "). Using default: " + Settings.LOCALE.getDefaultValue().toLanguageTag());
    }
  }

  public String getMessage(String key) {
    return messages.get(key);
  }
}
