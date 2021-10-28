package edu.whimc.journey.common.util;

import java.util.regex.Pattern;

/**
 * A utility class used to manage static classes that validate input.
 */
public final class Validator {

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
    return !Pattern.matches("^[a-zA-Z][a-zA-Z0-9 -]{1,30}[a-zA-Z0-9]$", name);
  }

}
