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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.proxy.JourneyChunk;
import net.whimxiqal.journey.proxy.UnavailableJourneyChunk;
import net.whimxiqal.journey.search.PathTrial;

/**
 * Wrapper of a {@link ChunkCache} that may be accessed through thread-safe methods.
 * Requests are made to the server thread and completed when the server-thread processes the request
 * and gives a read-only copy of the world chunk.
 */
public class CentralChunkCache {

  private static final int TICKS_PER_DEBUG_LOG = 20;  // once per second
  private final Map<ChunkId, ChunkRequest> requestMap = new HashMap<>();  // this tracks requests keyed by chunk id
  /**
   * Tracks completed requests in order of appearance.
   * This is only used on main thread, so no locking is needed for this
   */
  private final Queue<JourneyChunk> completedRequestQueue = new LinkedList<>();
  private final Object lock = new Object();
  private boolean enabled = true;
  private boolean chunkGeneration = false;
  private ChunkCache chunkCache = null;
  private UUID requestTaskId = null;
  private UUID loggingTaskId = null;

  // Counters
  private int addedCounter = 0;
  private int removedCounter = 0;

  /**
   * Start the repeated task that manages all cache requests.
   * Call on the main thread.
   */
  public void initialize() {
    chunkCache = new ChunkCache(PathTrial.MAX_CACHED_CHUNKS_PER_SEARCH * Settings.MAX_SEARCHES.getValue());
    requestTaskId = Journey.get().proxy().schedulingManager().scheduleRepeat(this::executeRequests,
        false, 1);  // Once per tick
    loggingTaskId = Journey.get().proxy().schedulingManager().scheduleRepeat(this::broadcastLogs,
        false, TICKS_PER_DEBUG_LOG);
    enabled = true;
    chunkGeneration = Settings.ALLOW_CHUNK_GENERATION.getValue();
  }

  /**
   * Stop the repeated tasks that manage this logic internally.
   * Call on the main thread.
   */
  public void shutdown() {
    Journey.logger().debug("[Chunk Cache] Shutting down...");
    if (requestTaskId != null) {
      Journey.get().proxy().schedulingManager().cancelTask(requestTaskId);
    }
    if (loggingTaskId != null) {
      Journey.get().proxy().schedulingManager().cancelTask(loggingTaskId);
      broadcastLogs();  // broadcast one last time
    }
    synchronized (lock) {
      enabled = false;
      for (Map.Entry<ChunkId, ChunkRequest> requestEntry : requestMap.entrySet()) {
        requestEntry.getValue().future().complete(new UnavailableJourneyChunk(requestEntry.getKey()));
      }
      requestMap.clear();
    }
    completedRequestQueue.clear();
  }

  /**
   * Runs on main server thread
   */
  private void executeRequests() {
    synchronized (lock) {
      // Prune any outdated chunks
      removedCounter += chunkCache.prune();

      // Execute requests
      int completed = 0;
      while (!completedRequestQueue.isEmpty()) {
        JourneyChunk chunk = completedRequestQueue.remove();
        ChunkRequest req = requestMap.remove(chunk.id());

        // This is all on the server thread, so we can convert to a real chunk safely here
        removedCounter += chunkCache.save(chunk);
        req.future().complete(chunk);
        completed++;
      }
      addedCounter += completed;
    }
  }

  /**
   * Runs on main server thread
   */
  private void broadcastLogs() {
    synchronized (lock) {
      if (addedCounter != 0 || removedCounter != 0) {
        Journey.logger().debug(String.format("[Chunk Cache] {%d}: added: %d, removed: %d",
            chunkCache.size(), addedCounter, removedCounter));
        addedCounter = 0;
        removedCounter = 0;
      }
    }
  }

  /**
   * Get a {@link Future} for a chunk given its id.
   * The future may complete automatically if it is already available in the cache.
   * Otherwise, a request will be submitted and upon the next server tick, a chunk will be provided.
   *
   * @param chunkId the id of the chunk
   * @return the chunk's future
   */
  public Future<JourneyChunk> getChunk(ChunkId chunkId) {
    synchronized (lock) {
      if (!enabled) {
        // we are shutdown, so just return a blank chunk
        return CompletableFuture.completedFuture(new UnavailableJourneyChunk(chunkId));
      }
      Future<JourneyChunk> request = null;
      // Request chunks for chunks surrounding the requested one, since they may be wanted later
      int chunkX = chunkId.x();
      int chunkZ = chunkId.z();
      for (int x = chunkX - 2; x <= chunkX + 2; x++) {
        for (int z = chunkZ - 2; z <= chunkZ + 2; z++) {
          boolean isRequestedChunk = x == chunkX && z == chunkZ;
          ChunkId innerChunkId = new ChunkId(chunkId.domain(), x, z);

          // Is this chunk already stored in cache?
          JourneyChunk maybeChunk = chunkCache.getChunk(innerChunkId);
          if (maybeChunk != null) {
            if (isRequestedChunk) {
              request = CompletableFuture.completedFuture(maybeChunk);
            }
            continue;
          }

          // Chunk is not stored in cache. Is it already queued?
          ChunkRequest maybeRequest = requestMap.get(innerChunkId);
          if (maybeRequest != null) {
            if (isRequestedChunk) {
              request = maybeRequest.future();
            }
            continue;
          }

          // Not stored and not queued. Queue it.
          maybeRequest = new ChunkRequest(innerChunkId);
          Journey.get().proxy().platform().toChunk(innerChunkId, chunkGeneration).thenAccept(completedRequestQueue::add);
          requestMap.put(innerChunkId, maybeRequest);
          if (isRequestedChunk) {
            // This is the actually requested one
            request = maybeRequest.future();
          }
        }
      }

      if (request == null) {
        throw new RuntimeException();  // programmer error -- we must have gotten a request at this point
      }
      return request;
    }
  }
}
