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

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A utility class used to manage static classes that validate input.
 */
public final class Validator {

  private static final String VALID_DATA_REGEX = "^[a-zA-Z0-9 -_]{1,30}$";
  /**
   * Characters allowed in chat/command scope destination keys (must be storable in Journey identifiers).
   * Notably excludes punctuation like '?' that display labels sometimes contain.
   */
  private static final Pattern SAFE_SCOPE_KEY = Pattern.compile("^[a-zA-Z0-9 _\\-:,.]+$");
  private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

  private Validator() {
  }

  /**
   * Check if the name is of the valid data name form.
   * It should start with a letter, then be a series letters, numbers, spaces, or dashes,
   * then end with a letter or a number.
   *
   * @param name the name to check
   * @return true if it is valid
   */
  public static boolean isInvalidDataName(String name) {
    if (name.equalsIgnoreCase("help")) {
      return true;
    }
    return !Pattern.matches(VALID_DATA_REGEX, name);
  }

  /**
   * Check if the display label for a waypoint is invalid.
   * Display names allow a wider character set than machine ids.
   *
   * @param name the display name to check
   * @return true if it is invalid
   */
  public static boolean isInvalidDisplayName(String name) {
    if (name.isBlank() || name.equalsIgnoreCase("help")) {
      return true;
    }
    return name.length() > 255;
  }

  /**
   * Normalize a waypoint name for fuzzy matching: lowercase, collapse spaces/dashes/punctuation
   * to underscores. So {@code "Sunken Forest?"}, {@code "sunken-forest"}, and
   * {@code "sunken_forest"} all become {@code "sunken_forest"}.
   */
  public static String normalizeWaypointKey(String name) {
    if (name == null || name.isBlank()) {
      return "";
    }
    String normalized = NON_ALNUM.matcher(name.toLowerCase(Locale.ENGLISH)).replaceAll("_");
    int start = 0;
    int end = normalized.length();
    while (start < end && normalized.charAt(start) == '_') {
      start++;
    }
    while (end > start && normalized.charAt(end - 1) == '_') {
      end--;
    }
    return normalized.substring(start, end);
  }

  /**
   * Whether a string is safe to use as a Journey scope / chat-click destination key.
   */
  public static boolean isSafeScopeKey(String key) {
    return key != null && !key.isBlank() && SAFE_SCOPE_KEY.matcher(key).matches() && !isInvalidDataName(key);
  }

}
