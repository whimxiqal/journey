package edu.whimc.indicator.common.util;

public final class Extra {

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

  private Extra() {
  }

}
