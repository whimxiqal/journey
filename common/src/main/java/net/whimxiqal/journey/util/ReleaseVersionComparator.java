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
