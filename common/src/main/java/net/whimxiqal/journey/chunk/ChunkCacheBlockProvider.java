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

package net.whimxiqal.journey.chunk;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.proxy.JourneyBlock;
import net.whimxiqal.journey.proxy.JourneyChunk;
import net.whimxiqal.journey.search.flag.FlagSet;

import static net.whimxiqal.journey.proxy.JourneyChunk.CHUNK_SIDE_LENGTH;

/**
 * Chunk cache that must be accessed single-threaded.
 */
public class ChunkCacheBlockProvider implements BlockProvider {


  private final ChunkCache chunkCache;
  private final FlagSet flagSet;

  public ChunkCacheBlockProvider(int maxCachedChunks, FlagSet flagSet) {
    this.chunkCache = new ChunkCache(maxCachedChunks);
    this.flagSet = flagSet;
  }

  @Override
  public JourneyBlock toBlock(Cell cell) throws ExecutionException, InterruptedException {
    // Clear any outdated items in the local cache
    chunkCache.prune();

    // Check the local cache for the chunk
    ChunkId chunkId = new ChunkId(cell.domain(), Math.floorDiv(cell.blockX(), CHUNK_SIDE_LENGTH), Math.floorDiv(cell.blockZ(), CHUNK_SIDE_LENGTH));
    int localX = Math.floorMod(cell.blockX(), CHUNK_SIDE_LENGTH);
    int localZ = Math.floorMod(cell.blockZ(), CHUNK_SIDE_LENGTH);
    JourneyChunk chunk = chunkCache.getChunk(chunkId);
    if (chunk == null) {
      // Not stored locally. We have to get the info from the central cache and cache the chunk locally for next time
      Future<JourneyChunk> future = Journey.get().centralChunkCache().getChunk(chunkId);
      chunk = future.get();  // waits for future. This is a source of latency and future versions should look into freeing up cycles while this is waiting
      chunkCache.save(chunk);
    }
    return chunk.block(localX, cell.blockY(), localZ, flagSet);
  }
}
