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

import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.whimxiqal.journey.Journey;

public abstract class MessageTemplate {

  private final static Pattern ELEMENT_KEY_SENTINEL_PATTERN = Pattern.compile("\\{([0-9])}");
  private final String key;

  MessageTemplate(String key) {
    this.key = key;
  }

  private String[] getMessageTokens(String message) {
    // split message into array of tokens, which contain both normal text and replacement terms
    //  i.e. ["The name ", "{0}", " could not be found"]
    return message.split("((?=" + ELEMENT_KEY_SENTINEL_PATTERN.pattern() + ")|(?<=" + ELEMENT_KEY_SENTINEL_PATTERN.pattern() + "))");
  }

  public final Component resolve(TextColor primaryColor, TextColor accentColor, boolean prefix, Object... elements) {
    // Check existence in message.yml
    Optional<String> configMessage = Journey.get().configManager().getMessage(key);
    if (configMessage.isPresent()) {
      return MessageManager.miniMessage().deserialize(configMessage.get(), TagResolver.resolver("param", ((argumentQueue, context) -> {
        if (!argumentQueue.hasNext()) {
          throw context.newException("Has no param argument specified", argumentQueue);
        }
        OptionalInt optionalInt = argumentQueue.pop().asInt();
        if (optionalInt.isEmpty()) {
          throw context.newException("Param argument is not an integer", argumentQueue);
        }
        Object element = elements[optionalInt.getAsInt()];
        if (element instanceof String) {
          return Tag.selfClosingInserting(Component.text((String) element));
        } else if (element instanceof Component) {
          return Tag.selfClosingInserting((Component) element);
        } else {
          throw new ClassCastException("Element " + element + " is not a String or Component");
        }
      })));
    } else {
      TextComponent.Builder builder = Component.text();
      if (prefix) {
        builder.append(Formatter.prefix());
      }
      String message = Journey.get().messageManager().getMessage(key);
      if (message == null) {
        throw new IllegalArgumentException("Unknown message key: " + key);
      }

      for (String token : getMessageTokens(message)) {
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
  }

  public String key() {
    return key;
  }

  public String rawMessage() {
    return Journey.get().messageManager().getMessage(key);
  }

  abstract int numElements();

}
