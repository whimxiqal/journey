/*
 * MIT License
 *
 * Copyright (c) 2020 Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package edu.whimc.indicator.util;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;

public final class Format {

  private Format() {
  }

  public static final ChatColor THEME = ChatColor.LIGHT_PURPLE;
  public static final ChatColor SUCCESS = ChatColor.GREEN;
  public static final ChatColor INFO = ChatColor.GOLD;
  public static final ChatColor WARN = ChatColor.YELLOW;
  public static final ChatColor ERROR = ChatColor.RED;
  public static final ChatColor STAFF = ChatColor.AQUA;
  public static final ChatColor ACCENT_1 = ChatColor.GOLD;
  public static final ChatColor ACCENT_2 = ChatColor.BLUE;
  public static final ChatColor DEFAULT = ChatColor.WHITE;
  public static final String PREFIX = THEME + "Indicator % " + ChatColor.RESET;

  public static String success(String message) {
    return PREFIX + SUCCESS + message;
  }

  public static String info(String message) {
    return PREFIX + INFO + message;
  }

  public static String warn(String message) {
    return PREFIX + WARN + message;
  }

  public static String error(String message) {
    return PREFIX + ERROR + message;
  }

  public static String formatInstantVerbose(Instant instant) {
    return new SimpleDateFormat("E, MMM dd yyyy, hh:mm aa zz").format(Date.from(instant));
  }

  public static Instant parseInstantVerbose(String source) throws ParseException {
    return new SimpleDateFormat("E, MMM dd yyyy, hh:mm aa zz").parse(source).toInstant();
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
