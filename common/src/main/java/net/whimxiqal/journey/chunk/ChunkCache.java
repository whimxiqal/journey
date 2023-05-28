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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import net.whimxiqal.journey.proxy.JourneyChunk;
import org.jetbrains.annotations.Nullable;

/**
 * A cache of {@link JourneyChunk}s, which are thread-safe objects to access block data.
 */
public final class ChunkCache {

  private static final long DEFAULT_CHUNK_SNAPSHOT_LIFETIME_MS = 10000;  // 10 seconds

  private final long chunkLifetimeMs;
  private final int maxCachedChunks;

  private final Map<ChunkId, JourneyChunk> chunkMap = new HashMap<>();                     // This tracks chunks keyed by chunk id
  private final Queue<DatedChunk> chunkQueue = new PriorityQueue<>(Comparator.comparing(DatedChunk::timestamp));  // This tracks chunks in order of appearance

  public ChunkCache(int maxCachedChunks) {
    this(maxCachedChunks, DEFAULT_CHUNK_SNAPSHOT_LIFETIME_MS);
  }

  public ChunkCache(int maxCachedChunks, long chunkLifetimeMs) {
    this.maxCachedChunks = maxCachedChunks;
    this.chunkLifetimeMs = chunkLifetimeMs;
  }

  /**
   * Remove any chunks that are marked for deletion.
   *
   * @return the number of chunks removed
   */
  public int prune() {
    long timeStampThreshold = System.currentTimeMillis() - chunkLifetimeMs;
    int pruned = 0;
    while (!chunkQueue.isEmpty()) {
      DatedChunk item = chunkQueue.peek();
      if (item.timestamp() > timeStampThreshold) {
        // We've already reached one that's young enough -- everything else will be younger, so stop pruning
        break;
      }
      chunkQueue.remove();
      chunkMap.remove(item.chunk().id());
      pruned++;
    }
    return pruned;
  }

  /**
   * Save a chunk to this cache.
   *
   * @param chunk the chunk
   * @return the number of chunks removed to make room for this chunk
   */
  public int save(JourneyChunk chunk) {
    // Make room for the new chunk
    int count = 0;
    while (chunkMap.size() >= maxCachedChunks) {
      DatedChunk removed = chunkQueue.remove();
      chunkMap.remove(removed.chunk().id());
      count++;
    }
    // size of chunkMap and queue must be less than maxCachedChunks

    ChunkId id = chunk.id();
    chunkMap.put(id, chunk);
    chunkQueue.add(DatedChunk.create(chunk));
    return count;
  }

  public int size() {
    return chunkMap.size();
    // (chunkQueue must also have this size)
  }

  @Nullable
  public JourneyChunk getChunk(ChunkId chunkId) {
    return chunkMap.get(chunkId);
  }

  @Override
  public String toString() {
    return "ChunkCache{" +
        "chunkMap size=" + chunkMap.size() +
        ", chunkList size=" + chunkQueue.size() +
        '}';
  }
}
