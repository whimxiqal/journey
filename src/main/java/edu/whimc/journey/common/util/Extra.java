package edu.whimc.journey.common.util;

import com.google.common.collect.Lists;
import java.util.LinkedList;

public final class Extra {

  private Extra() {
  }

  public static boolean isNumber(String s) {
    for (int c : s.toCharArray()) {
      if (c > '9' || c < '0') {
        return false;
      }
    }
    return true;
  }

  public static boolean isCommandOffset(String offsetString) {
    return !offsetString.isEmpty() && offsetString.charAt(0) == '~';
  }

  public static int calcCommandOffset(int origin, String offsetString) throws IllegalArgumentException {
    if (offsetString.isEmpty()) {
      throw new IllegalArgumentException("The offset string may not be empty");
    }
    if (!(offsetString.charAt(0) == '~')) {
      throw new IllegalArgumentException("The offset string must start with a tilde (~)");
    }
    String subString = offsetString.substring(1);
    int offset = subString.isEmpty() ? 0 : Integer.parseInt(offsetString.substring(1));
    return origin + offset;
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

  public static String quoteStringWithSpaces(String input) {
    if (input.contains(" ")) {
      return "\"" + input + "\"";
    } else {
      return input;
    }
  }

}
