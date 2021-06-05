package edu.whimc.indicator.common.util;

import java.util.regex.Pattern;

public final class Validator {

  /**
   * Check if the name is of the valid data name form.
   * It should start with a letter, then be a series letters, numbers, spaces, or dashes,
   * then end with a letter or a number.
   * @param name the name to check
   * @return true if it is valid
   */
  public static boolean isValidDataName(String name) {
    return Pattern.matches("^[a-zA-Z][a-zA-Z0-9 -]{1,30}[a-zA-Z0-9]$", name);
  }

  private Validator() {
  }

}
