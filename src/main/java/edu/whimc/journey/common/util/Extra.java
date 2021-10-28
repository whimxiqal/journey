package edu.whimc.journey.common.util;

import com.google.common.collect.Lists;
import java.util.LinkedList;

/**
 * A utility class to perform "extra" miscellaneous tasks.
 */
public final class Extra {

  private Extra() {
  }

  /**
   * Determine if a string is a number.
   *
   * @param s the string
   * @return true if it is a number
   */
  public static boolean isNumber(String s) {
    for (int c : s.toCharArray()) {
      if (c > '9' || c < '0') {
        return false;
      }
    }
    return true;
  }

  /**
   * Combine an array of single-word inputs into another array where the words
   * surrounded by quotes are combined into singular strings within the array.
   *
   * @param input the inputs
   * @return the combined inputs
   */
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

  /**
   * Place quotes around the input if there is a space in it.
   *
   * @param input the input string
   * @return the new string
   */
  public static String quoteStringWithSpaces(String input) {
    if (input.contains(" ")) {
      return "\"" + input + "\"";
    } else {
      return input;
    }
  }

}
