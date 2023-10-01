package net.whimxiqal.journey.util;

import java.util.Comparator;

public class ReleaseVersionComparator implements Comparator<String> {

  private static final String VERSION_TOKENIZER_DELIMITER = "[.-]";

  @Override
  public int compare(String version1, String version2) {
    String[] version1Tokens = version1.split(VERSION_TOKENIZER_DELIMITER);
    String[] version2Tokens = version2.split(VERSION_TOKENIZER_DELIMITER);
    for (int i = 0; i < Math.min(version1Tokens.length, version2Tokens.length); i++) {
      int tokenComparison = version1Tokens[i].compareTo(version2Tokens[i]);
      if (tokenComparison < 0) {
        return -1;
      } else if (tokenComparison > 0) {
        return 1;
      }
    }

    // If all tokens are the same up until the minimum length of the token arrays,
    // then the one with the smaller number of tokens is decided as higher/more new
    // i.e. v1.2.0 > v1.2.0-dev, but this should generally not happen in production
    return -Integer.compare(version1Tokens.length, version2Tokens.length);
  }

}
