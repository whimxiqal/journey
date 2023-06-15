package net.whimxiqal.journey.proxy;

import net.whimxiqal.journey.chunk.ChunkId;
import net.whimxiqal.journey.search.flag.FlagSet;

public record UnavailableJourneyChunk(ChunkId id) implements JourneyChunk {
  @Override
  public JourneyBlock block(int x, int y, int z, FlagSet flagSet) {
    return new UnavailableJourneyBlock(JourneyChunk.toCell(id, x, y, z));
  }
}
