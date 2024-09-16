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

package net.whimxiqal.journey.util;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.whimxiqal.journey.Color;

public final class ColorUtil {

  private static final Pattern HEX_VALUE_PATTERN = Pattern.compile("^[0-9a-fA-F]{6}$");
  private static final int MAX_COLOR_VALUE = 255;

  public static Color fromHex(String hex) throws ParseException {
    Matcher matcher = HEX_VALUE_PATTERN.matcher(hex);
    if (!matcher.matches()) {
      throw new ParseException("Expected hex value, got: " + hex, 0);
    }
    String rawHex = matcher.group(0);
    int red = Integer.valueOf(rawHex.substring(0, 2), 16);
    int green = Integer.valueOf(rawHex.substring(2, 4), 16);
    int blue = Integer.valueOf(rawHex.substring(4, 6), 16);
    return new Color(red, green, blue);
  }

  public static boolean valid(Color color) {
    return color.red() >= 0 && color.red() <= MAX_COLOR_VALUE &&
        color.green() >= 0 && color.green() <= MAX_COLOR_VALUE &&
        color.blue() >= 0 && color.blue() <= MAX_COLOR_VALUE;
  }

  private ColorUtil() {
  }

}
