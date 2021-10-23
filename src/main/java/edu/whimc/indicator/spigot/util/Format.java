/*
 * Copyright 2021 Pieter Svenson
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

package edu.whimc.indicator.spigot.util;

import edu.whimc.indicator.spigot.navigation.LocationCell;
import java.util.Arrays;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Format {

  public static final ChatColor THEME = ChatColor.LIGHT_PURPLE;
  public static final ChatColor SUCCESS = ChatColor.GREEN;
  public static final ChatColor INFO = ChatColor.GOLD;
  public static final ChatColor WARN = ChatColor.YELLOW;
  public static final ChatColor ERROR = ChatColor.RED;
  public static final ChatColor DEBUG = ChatColor.AQUA;
  public static final ChatColor ACCENT = ChatColor.LIGHT_PURPLE;
  public static final ChatColor ACCENT2 = ChatColor.DARK_AQUA;
  public static final ChatColor DEFAULT = ChatColor.WHITE;
  public static final String PREFIX = THEME + "Indicator " + ChatColor.DARK_GRAY + "% " + ChatColor.RESET;

  private Format() {
  }

  public static String toPlain(BaseComponent[] text) {
    StringBuilder builder = new StringBuilder();
    for (BaseComponent component : text) {
      builder.append(component.toPlainText());
    }
    return builder.toString();
  }

  public static BaseComponent[] textOf(String single) {
    return new BaseComponent[]{new TextComponent(single)};
  }

  public static BaseComponent[] applyColorToAllOf(ChatColor color, String... message) {
    StringBuilder builder = new StringBuilder();
    for (Object m : message) {
      builder.append(color).append(m);
    }
    return textOf(builder.toString());
  }

  public static BaseComponent[] success(String... message) {
    return chain(textOf(PREFIX), applyColorToAllOf(SUCCESS, message));
  }

  public static BaseComponent[] info(String... message) {
    return chain(textOf(PREFIX), applyColorToAllOf(INFO, message));
  }

  public static BaseComponent[] warn(String... message) {
    return chain(textOf(PREFIX), applyColorToAllOf(WARN, message));
  }

  public static BaseComponent[] error(String... message) {
    return chain(textOf(PREFIX), applyColorToAllOf(ERROR, message));
  }

  public static BaseComponent[] debug(String... message) {
    return chain(textOf(PREFIX), applyColorToAllOf(DEBUG, message));
  }

  public static BaseComponent[] note(String... message) {
    return applyColorToAllOf(DEFAULT, message);
  }

  public static BaseComponent[] locationCell(LocationCell cell, ChatColor defaultColor) {
    return applyColorToAllOf(defaultColor,
        "[",
        Format.ACCENT + "" + cell.getX(),
        ", ",
        Format.ACCENT + "" + cell.getY(),
        ", ",
        Format.ACCENT + "" + cell.getZ(),
        " in ",
        Format.ACCENT + cell.getDomain().getName(),
        "]");
  }

  public static BaseComponent[] chain(BaseComponent[]... arrays) {
    return Arrays.stream(arrays)
        .flatMap(Arrays::stream)
        .toArray(BaseComponent[]::new);
  }

  public static BaseComponent[] command(@NotNull String command, @Nullable String description) {
    return command(command, command, description);
  }

  public static BaseComponent[] command(@NotNull String label, @NotNull String command, @Nullable String description) {
    return new ComponentBuilder()
        .append(Format.INFO + "[")
        .append(Format.ACCENT + label)
        .italic(true)
        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new Text((description == null || description.isEmpty())
                ? command
                : description + "\n\n" + command)))
        .append(Format.INFO + "]")
        .italic(false)
        .create();
  }
}
