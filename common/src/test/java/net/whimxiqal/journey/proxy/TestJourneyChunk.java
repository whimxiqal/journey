package net.whimxiqal.journey.proxy;

import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.chunk.ChunkId;
import net.whimxiqal.journey.search.flag.FlagSet;

public record TestJourneyChunk(ChunkId chunkId) implements JourneyChunk {

  @Override
  public ChunkId id() {
    return chunkId;
  }

  @Override
  public JourneyBlock block(int x, int y, int z, FlagSet flagSet) {
    return new TestJourneyBlock(new Cell((chunkId.x() * 16) + x, y, chunkId.z() * 16 + z, chunkId.domain()));
  }

}
