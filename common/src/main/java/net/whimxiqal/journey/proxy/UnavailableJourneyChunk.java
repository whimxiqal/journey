package net.whimxiqal.journey.proxy;

import net.whimxiqal.journey.chunk.ChunkId;
import net.whimxiqal.journey.search.flag.FlagSet;

public class UnavailableJourneyChunk extends JourneyChunk {
  public UnavailableJourneyChunk(ChunkId chunkId) {
    super(chunkId);
  }

  @Override
  public JourneyBlock realBlock(int x, int y, int z, FlagSet flagSet) {
    return new UnavailableJourneyBlock(toCell(x, y, z));
  }
}
