package net.whimxiqal.journey.util;

import java.util.LinkedList;
import java.util.List;

public final class StringUtil {

  public static List<String> segmentPhrase(String phrase, int maxSegmentLength) {
    List<String> segments = new LinkedList<>();
    String[] tokens = phrase.split("[ \t\n]");

    List<String> tokenBuffer = new LinkedList<>();
    int bufferSize = 0;
    int i = 0;
    while (i < tokens.length) {
      int currentSegmentLength = bufferSize + tokenBuffer.size() - 1;
      String token = tokens[i];
      if (tokenBuffer.isEmpty() || currentSegmentLength + token.length() <= maxSegmentLength) {
        tokenBuffer.add(token);
        bufferSize += token.length();
        i++;
        continue;
      }
      segments.add(String.join(" ", tokenBuffer));
      tokenBuffer.clear();
      bufferSize = 0;
    }
    if (!tokenBuffer.isEmpty()) {
      segments.add(String.join(" ", tokenBuffer));
    }
    return segments;
  }

  private StringUtil() {
  }
}
