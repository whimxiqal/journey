package net.whimxiqal.journey.chunk;

import net.whimxiqal.journey.proxy.JourneyChunk;

/**
 * A chunk that has a timestamp for when it was created.
 * @param chunk the chunk
 * @param timestamp the time it was created, in milliseconds
 */
record DatedChunk(JourneyChunk chunk, long timestamp) {

  public static DatedChunk create(JourneyChunk chunk) {
    return new DatedChunk(chunk, System.currentTimeMillis());
  }

}
