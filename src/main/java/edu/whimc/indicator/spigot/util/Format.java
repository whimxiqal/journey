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

import com.google.common.collect.Lists;
import edu.whimc.indicator.spigot.path.LocationCell;
import org.bukkit.ChatColor;

import java.util.LinkedList;

public final class Format {

  private Format() {
  }

  public static final ChatColor THEME = ChatColor.LIGHT_PURPLE;
  public static final ChatColor SUCCESS = ChatColor.GREEN;
  public static final ChatColor INFO = ChatColor.GOLD;
  public static final ChatColor WARN = ChatColor.YELLOW;
  public static final ChatColor ERROR = ChatColor.RED;
  public static final ChatColor DEBUG = ChatColor.AQUA;
  public static final ChatColor ACCENT = ChatColor.LIGHT_PURPLE;
  public static final ChatColor ACCENT2 = ChatColor.DARK_AQUA;
  public static final ChatColor DEFAULT = ChatColor.WHITE;
  public static final String PREFIX = THEME + "Indicator % " + ChatColor.RESET;

  public static String applyColorToAllOf(ChatColor color, Object... message) {
    StringBuilder builder = new StringBuilder();
    for (Object m : message) {
      builder.append(color).append(m);
    }
    return builder.toString();
  }

  public static String success(Object... message) {
    return PREFIX + applyColorToAllOf(SUCCESS, message);
  }

  public static String info(Object... message) {
    return PREFIX + applyColorToAllOf(INFO, message);
  }

  public static String warn(Object... message) {
    return PREFIX + applyColorToAllOf(WARN, message);
  }

  public static String error(Object... message) {
    return PREFIX + applyColorToAllOf(ERROR, message);
  }

  public static String debug(Object... message) {
    return PREFIX + applyColorToAllOf(DEBUG, message);
  }

  public static String note(Object... message) {
    return applyColorToAllOf(DEFAULT, message);
  }

  public static String locationCell(LocationCell cell, ChatColor defaultColor) {
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

  public static String[] combineQuotedArguments(String[] input) {
    String full = String.join(" ", input);
    LinkedList<String> out = Lists.newLinkedList();
    boolean inEncloser = false;
    StringBuilder arg = new StringBuilder();
    for (char c : full.toCharArray()) {
      if (c == ' ') {
        if (inEncloser) {
          arg.append(c);
        } else {
          out.add(arg.toString());
          arg = new StringBuilder();
        }
      } else if (c == '"') {
        inEncloser = !inEncloser;
        if (!arg.toString().isEmpty()) {
          out.add(arg.toString());
        }
        arg = new StringBuilder();
      } else {
        arg.append(c);
      }
    }
    if (!arg.toString().isEmpty()) {
      out.add(arg.toString());
    }
    return out.toArray(new String[0]);
  }

}
