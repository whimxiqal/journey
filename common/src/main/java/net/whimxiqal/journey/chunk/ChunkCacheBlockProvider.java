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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.proxy.JourneyBlock;
import net.whimxiqal.journey.proxy.JourneyChunk;
import net.whimxiqal.journey.search.AbstractPathTrial;
import net.whimxiqal.journey.search.flag.FlagSet;

/**
 * Chunk cache that must be accessed single-threaded.
 */
public class ChunkCacheBlockProvider implements BlockProvider {

  public static final int CHUNK_SIDE_LENGTH = 16;

  private final ChunkCache chunkCache;
  private final int maxCachedChunks;
  private final FlagSet flagSet;

  public ChunkCacheBlockProvider(int maxCachedChunks, FlagSet flagSet) {
    this.chunkCache = new ChunkCache(maxCachedChunks);
    this.maxCachedChunks = maxCachedChunks;
    this.flagSet = flagSet;
  }

  /**
   * Prepare some chunks between origin and destination.
   *
   * @param origin      the starting point of where to start preparing chunks
   * @param destination the end point
   */
  public void prepareChunks(Cell origin, Cell destination, int maxChunks) {
    if (origin.domain() != destination.domain()) {
      throw new IllegalArgumentException("Origin and destination did not have the same domain");
    }

    // adjust the actual max because we don't want to do more than our cache can actually handle
    int adjustedMaxChunks = Math.min(maxChunks, maxCachedChunks / 2);

    double xDiff = destination.blockX() - origin.blockX();
    double zDiff = destination.blockZ() - origin.blockZ();

    final double distance = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
    final double xUnitComp = xDiff / distance;
    final double zUnitComp = zDiff / distance;

    // Walk one block at a time directly towards the destination and queue any necessary chunks
    int lastChunkX = Integer.MIN_VALUE;
    int lastChunkZ = Integer.MIN_VALUE;
    double curDist = 0;
    List<ChunkId> toQueue = new LinkedList<>();
    while (curDist < distance && toQueue.size() < adjustedMaxChunks) {
      int chunkX = Math.floorDiv((int) Math.floor(xUnitComp * curDist), CHUNK_SIDE_LENGTH);
      int chunkZ = Math.floorDiv((int) Math.floor(zUnitComp * curDist), CHUNK_SIDE_LENGTH);
      if (lastChunkX != chunkX || lastChunkZ != chunkZ) {
        toQueue.add(new ChunkId(origin.domain(), chunkX, chunkZ));
        lastChunkX = chunkX;
        lastChunkZ = chunkZ;
      }
      curDist += 1.0;
    }

    Journey.get().getCentralChunkCache().loadChunks(toQueue);
  }

  @Override
  public JourneyBlock getBlock(Cell cell) throws ExecutionException, InterruptedException {
    // Clear any outdated items in the local cache
    chunkCache.prune();

    // Check the local cache for the chunk
    ChunkId chunkId = new ChunkId(cell.domain(), Math.floorDiv(cell.blockX(), CHUNK_SIDE_LENGTH), Math.floorDiv(cell.blockZ(), CHUNK_SIDE_LENGTH));
    int localX = Math.floorMod(cell.blockX(), CHUNK_SIDE_LENGTH);
    int localZ = Math.floorMod(cell.blockZ(), CHUNK_SIDE_LENGTH);
    JourneyChunk chunk = chunkCache.getChunk(chunkId);
    if (chunk == null) {
      // Not stored locally. We have to get the info from the central cache and cache the chunk locally for next time
      Future<JourneyChunk> future = Journey.get().getCentralChunkCache().getChunk(chunkId);
      chunk = future.get();  // waits for future, will take at most 1 game tick
      chunkCache.save(chunk);
    }
    return chunk.block(localX, cell.blockY(), localZ, flagSet);
  }
}
