package net.whimxiqal.journey.navigation.option;

import java.text.ParseException;

public class ParseNavigatorOptionException extends ParseException {

  public ParseNavigatorOptionException(String s, int errorOffset) {
    super(s, errorOffset);
  }

}
