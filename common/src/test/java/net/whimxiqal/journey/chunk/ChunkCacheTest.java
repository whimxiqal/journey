package net.whimxiqal.journey.chunk;

import java.util.concurrent.ExecutionException;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.JourneyTestHarness;
import net.whimxiqal.journey.search.flag.FlagSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChunkCacheTest extends JourneyTestHarness {

  void correctness(BlockProvider provider, Cell cell) throws ExecutionException, InterruptedException {
    Assertions.assertEquals(cell, provider.getBlock(cell).cell());
  }

  @Test
  void correctness() throws ExecutionException, InterruptedException {
    SynchronousChunkCache chunkCache = new SynchronousChunkCache(new FlagSet());

    correctness(chunkCache, new Cell(0, 0, 0, 0));
    correctness(chunkCache, new Cell(1, 2, 3, 4));
    correctness(chunkCache, new Cell(-1, -3, -5, 2));
    correctness(chunkCache, new Cell(101, -202, -303, 404));
    correctness(chunkCache, new Cell(-16, 0, -16, 1)); // on the edge of the chunk
  }

}