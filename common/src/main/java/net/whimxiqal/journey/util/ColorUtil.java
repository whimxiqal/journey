package net.whimxiqal.journey.util;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.whimxiqal.journey.navigation.option.Color;

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
