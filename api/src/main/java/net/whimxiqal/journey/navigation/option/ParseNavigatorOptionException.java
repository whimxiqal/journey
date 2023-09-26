package net.whimxiqal.journey.navigation.option;

import java.text.ParseException;

/**
 * An exception thrown when a string cannot be parsed when trying to read values for
 * navigator options.
 */
public class ParseNavigatorOptionException extends ParseException {

  /**
   * General constructor.
   *
   * @see ParseException#ParseException(String, int)
   */
  public ParseNavigatorOptionException(String s, int errorOffset) {
    super(s, errorOffset);
  }

}
