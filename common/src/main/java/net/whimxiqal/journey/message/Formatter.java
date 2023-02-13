/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.Cell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility class to store static fields and methods pertaining to
 * formatted messages with the purpose of sending meaningfully colored
 * and enhanced messages to players and other message receivers.
 */
public final class Formatter {

  public static final TextColor ACCENT = TextColor.color(20, 166, 219);  // #14A6DB
  public static final TextColor DARK = TextColor.color(64, 64, 64);
  public static final TextColor DULL = TextColor.color(179, 179, 179);
  public static final TextColor ERROR = TextColor.color(194, 12, 21);
  public static final String FORMAT_ACCENT_REGEX = "___";
  public static final TextColor GOLD = TextColor.color(222, 185, 0);
  public static final TextColor INFO = TextColor.color(255, 191, 245);
  public static final TextColor DEBUG = TextColor.color(128, 255, 233);
  public static final TextComponent SPACE = Component.text(" ");
  public static final TextColor SUCCESS = TextColor.color(11, 181, 38);
  public static final TextColor THEME = TextColor.color(172, 21, 219);  // #AC15DB
  public static final TextComponent TWO_SPACES = Component.text("  ");
  public static final TextColor URL = TextColor.color(66, 105, 224);
  public static final TextColor WARN = TextColor.color(255, 157, 10);
  public static final Component WELCOME = Component.text()
      .append(Component.text("%%%%%%%%  ").color(DULL))
      .append(Component.text("Journey ").color(THEME))
      .append(Component.text("v" + Journey.get().proxy().version()).color(DARK))
      .append(Component.text("  %%%%%%%%").color(DULL))
      .append(Component.newline())
      .append(Component.text("            by ").color(DULL))
      .append(Component.text("whimxiqal").color(ACCENT))
      .append(Component.newline())
      .append(url(" github.com/whimxiqal/journey", "https://github.com/whimxiqal/journey"))
      .build();

  public static final TextColor WHITE = TextColor.color(255, 255, 255);

  /**
   * Return the plugin's standard prefix text, to prepend to some general text
   * sent on behalf of the plugin.
   *
   * @return the text component
   */
  public static Component prefix() {
    return Component.text()
        .append(Component.text("Journey ").color(THEME))
        .append(Component.text("% ").color(DARK))
        .build();
  }

  public static Component formattedMessage(TextColor color, String message,
                                            boolean prefix, Object... accented) {
    LinkedList<String> tokenList = new LinkedList<>(Arrays.asList(message.split(FORMAT_ACCENT_REGEX, -1)));

    TextComponent.Builder builder = Component.text();
    if (prefix) {
      builder.append(prefix());
    }
    for (int i = 0; i < tokenList.size() - 1; i++) {
      builder.append(Component.text(tokenList.get(i), color));
      if (accented[i] instanceof Component) {
        builder.append((Component) accented[i]);
      } else {
        builder.append(Component.text(accented[i].toString(), ACCENT));
      }
    }
    builder.append(Component.text(tokenList.getLast(), color));
    return builder.build();
  }

  public static Component success(String message) {
    return success(Component.text(message));
  }

  public static Component success(String message, Object... insertions) {
    return formattedMessage(SUCCESS, message, true, insertions);
  }

  public static Component success(Component message) {
    return prefix().append(message).color(SUCCESS);
  }

  public static Component error(String message) {
    return error(Component.text(message));
  }

  public static Component error(String message, Object... insertions) {
    return formattedMessage(ERROR, message, true, insertions);
  }

  public static Component error(Component message) {
    return prefix().append(message).color(ERROR);
  }

  public static Component warn(String message) {
    return warn(Component.text(message));
  }

  public static Component warn(String message, Object... insertions) {
    return formattedMessage(WARN, message, true, insertions);
  }

  public static Component warn(Component message) {
    return prefix().append(message).color(WARN);
  }

  public static Component info(String message) {
    return info(Component.text(message));
  }

  public static Component info(String message, Object... insertions) {
    return formattedMessage(INFO, message, true, insertions);
  }

  public static Component info(Component message) {
    return prefix().append(message).color(INFO);
  }

  public static Component debug(String message) {
    return debug(Component.text(message));
  }

  public static Component debug(String message, Object... insertions) {
    return formattedMessage(DEBUG, message, true, insertions);
  }

  public static Component debug(Component message) {
    return prefix().append(message).color(DEBUG);
  }

  public static Component accent(String message) {
    return Component.text(message).color(ACCENT);
  }

  public static Component accent(String message, Object... insertions) {
    return formattedMessage(WHITE, message, false, insertions);
  }

  public static Component keyValue(String key, String value) {
    return keyValue(key, Component.text(value));
  }

  public static Component keyValue(String key, Component value) {
    return keyValue(ACCENT, key, value);
  }

  public static Component keyValue(TextColor keyColor, String key, String value) {
    return keyValue(keyColor, key, Component.text(value));
  }

  /**
   * Format a key-value pair.
   *
   * @param keyColor the color of the key
   * @param key      the key
   * @param value    the value
   * @return the text component
   */
  public static Component keyValue(TextColor keyColor, String key, Component value) {
    return Component.text()
        .append(Component.text(key).color(keyColor))
        .append(Component.text(" "))
        .append(value)
        .build();
  }

  /**
   * Format some text to display text when the user hovers over it.
   *
   * @param label   the text to show in chat
   * @param onHover the text to display on hover
   * @return the text component
   */
  public static Component hover(String label, String onHover) {
    return hover(Component.text(label).decorate(TextDecoration.ITALIC),
        Component.text(onHover));
  }

  public static Component hover(Component label, Component onHover) {
    return label.hoverEvent(HoverEvent.showText(onHover));
  }

  /**
   * Format some text to link to a given url.
   *
   * @param label the text to show in the chat
   * @param url   the url to link to on click
   * @return the component text
   */
  public static Component url(@NotNull String label, @NotNull String url) {
    TextComponent.Builder textBuilder = Component.text();
    textBuilder.append(Component.text(label).color(URL));
    textBuilder.hoverEvent(HoverEvent.showText(Component.text(url)));
    try {
      textBuilder.clickEvent(ClickEvent.openUrl(new URL(url)));
    } catch (MalformedURLException ex) {
      textBuilder.clickEvent(ClickEvent.suggestCommand(url));
      Journey.get().proxy().logger().error("A url was not formed correctly for a"
          + " click action: " + url);
    }

    return textBuilder.build();
  }

  public static Component commandSuggest(@NotNull String label,
                                         @NotNull String command,
                                         @Nullable Component hoverMessage) {
    return command(label, command, hoverMessage, true, true);
  }

  public static Component command(@NotNull String label,
                                  @NotNull String command,
                                  @Nullable Component hoverMessage) {
    return command(label, command, hoverMessage, true, false);
  }

  /**
   * Format text to run a command when clicked.
   *
   * @param label        the text to show in the chat
   * @param command      the command to run when the text is clicked
   * @param hoverMessage the text to show when hovered
   * @param accentuate   whether to make the text stand out in the chat window
   * @param suggest      whether to suggest the command instead of running it immediately
   * @return the text component
   */
  public static Component command(@NotNull String label,
                                  @NotNull String command,
                                  @Nullable Component hoverMessage,
                                  boolean accentuate,
                                  boolean suggest) {
    Component labelText = Component.text(label).color(ACCENT);
    if (accentuate) {
      labelText = Component.text()
          .append(Component.text("[").color(GOLD))
          .append(labelText)
          .append(Component.text("]").color(GOLD))
          .build();
    }

    TextComponent.Builder builder = Component.text()
        .append(labelText)
        .clickEvent(suggest
            ? ClickEvent.suggestCommand(command)
            : ClickEvent.runCommand(command));

    if (hoverMessage != null) {
      builder.hoverEvent(HoverEvent.showText(hoverMessage.color(ACCENT)
          .append(Component.text("\n" + command).color(DULL))));
    }

    return builder.build();
  }

  public static Component dull(String s) {
    return formattedMessage(DULL, s, false);
  }

  /**
   * Cast an object to a component. It can either be a {@link Component}
   * or a {@link String}.
   *
   * @param o the object to cast
   * @return the text component
   */
  public static Component castToComponent(Object o) {
    if (o instanceof Component) {
      return (Component) o;
    } else if (o instanceof String) {
      return Component.text((String) o);
    } else {
      throw new IllegalArgumentException("Unknown type tried to cast to Component: "
          + o.getClass().getSimpleName());
    }
  }

  public static Component cell(Cell cell) {
    return Component.text()
        .append(Component.text("[x: ").color(DULL))
        .append(Component.text(cell.blockX()).color(ACCENT))
        .append(Component.text(", y: ").color(DULL))
        .append(Component.text(cell.blockY()).color(ACCENT))
        .append(Component.text(", z: ").color(DULL))
        .append(Component.text(cell.blockZ()).color(ACCENT))
        .append(Component.text("] (").color(DULL))
        .append(Component.text(Journey.get().proxy().platform().worldIdToName(cell.domainId())).color(ACCENT))
        .append(Component.text(")").color(DULL))
        .build();
  }

  public static Component noPlayer(String name) {
    return error("No player found with name ___", name);
  }
}
