package net.whimxiqal.journey.message;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.whimxiqal.journey.Journey;

public abstract class MessageTemplate {

  private final static Pattern ELEMENT_KEY_SENTINEL_PATTERN = Pattern.compile("\\{([0-9])}");
  private final String key;

  MessageTemplate(String key) {
    this.key = key;
  }

  public final Component resolve(TextColor primaryColor, TextColor accentColor, boolean prefix, Object... elements) {
    TextComponent.Builder builder = Component.text();
    if (prefix) {
      builder.append(Formatter.prefix());
    }
    String message = Journey.get().messageManager().getMessage(key);
    if (message == null) {
      throw new IllegalArgumentException("Unknown message key: " + key);
    }

    // split message into array of tokens, which contain both normal text and replacement terms
    //  i.e. ["The name ", "{0}", " could not be found"]
    String[] tokens = message.split("((?=" + ELEMENT_KEY_SENTINEL_PATTERN.pattern() + ")|(?<=" + ELEMENT_KEY_SENTINEL_PATTERN.pattern() + "))");
    for (String token : tokens) {
      Matcher matcher = ELEMENT_KEY_SENTINEL_PATTERN.matcher(token);
      if (matcher.matches()) {
        // this is a parameter sentinel, like {0}
        Object element = elements[Integer.parseInt(matcher.group(1))];
        if (element instanceof String) {
          builder.append(Component.text((String) element).color(accentColor));
        } else if (element instanceof Component) {
          builder.append((Component) element);
        } else {
          throw new ClassCastException("Element " + element + " is not a String or Component");
        }
      } else {
        // this is not a parameter sentinel
        builder.append(Component.text(token));
      }
    }
    builder.color(primaryColor);
    return builder.build();
  }

  protected final String resolve(String... elements) {
    StringBuilder builder = new StringBuilder();
    String raw = rawMessage();
    if (raw == null) {
      throw new IllegalArgumentException("Unknown message key: " + key);
    }

    // split message into array of tokens, which contain both normal text and replacement terms
    //  i.e. ["The name ", "{0}", " could not be found"]
    String[] tokens = raw.split("((?=" + ELEMENT_KEY_SENTINEL_PATTERN.pattern() + ")|(?<=" + ELEMENT_KEY_SENTINEL_PATTERN.pattern() + "))");
    for (String token : tokens) {
      Matcher matcher = ELEMENT_KEY_SENTINEL_PATTERN.matcher(token);
      if (matcher.matches()) {
        // this is a parameter sentinel, like {0}
        builder.append(elements[Integer.parseInt(matcher.group(1))]);
      } else {
        // this is not a parameter sentinel
        builder.append(token);
      }
    }
    return builder.toString();
  }

  public String key() {
    return key;
  }

  public String rawMessage() {
    return Journey.get().messageManager().getMessage(key);
  }

  abstract int numElements();

}
