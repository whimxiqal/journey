package net.whimxiqal.journey.chunk;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.message.Formatter;
import net.whimxiqal.journey.proxy.JourneyChunk;

/**
 * Thread-safe cache of chunks.
 */
public class CentralChunkCache {

  public static final int MAX_CACHED_CHUNKS_PER_SEARCH = 128;
  private final Map<ChunkId, ChunkRequest> requestMap = new HashMap<>();  // this tracks requests keyed by chunk id
  private final Queue<ChunkRequest> requestQueue = new LinkedList<>();  // this tracks requests in order of appearance
  private final Object lock = new Object();
  private ChunkCache chunkCache = null;
  private UUID requestTaskId = null;
  private UUID loggingTaskId = null;

  // Counters
  private int retrieved = 0;
  private int replaced = 0;
  private int pruned = 0;

  /**
   * Start the repeated task that manages all cache requests.
   * Call on the main thread.
   */
  public void initialize() {
    chunkCache = new ChunkCache(MAX_CACHED_CHUNKS_PER_SEARCH * Settings.MAX_SEARCHES.getValue());
    requestTaskId = Journey.get().proxy().schedulingManager().scheduleRepeat(this::executeRequests,
        false, 1);  // Once per tick
    loggingTaskId = Journey.get().proxy().schedulingManager().scheduleRepeat(this::broadcastLogs,
        false, 20); // Once per second
  }

  /**
   * Stop the repeated tasks that manage this logic internally.
   * Call on the main thread.
   */
  public void shutdown() {
    if (requestTaskId != null) {
      Journey.get().proxy().schedulingManager().cancelTask(requestTaskId);
    }
    if (loggingTaskId != null) {
      Journey.get().proxy().schedulingManager().cancelTask(loggingTaskId);
      broadcastLogs();  // broadcast one last time
    }
  }

  /**
   * Runs on main server thread
   */
  private void executeRequests() {
    // Prune any outdated chunks
    pruned += chunkCache.prune();

    // Execute requests
    retrieved += requestQueue.size();
    while (!requestQueue.isEmpty()) {
      ChunkRequest req = requestQueue.remove();
      requestMap.remove(req.chunkId());

      // This is all on the server thread, so we can convert to a real chunk safely here
      JourneyChunk snapshot = Journey.get().proxy().platform().toChunk(req.chunkId());
      replaced += chunkCache.save(snapshot);
      req.future().complete(snapshot);
    }
  }

  /**
   * Runs on main server thread
   */
  private void broadcastLogs() {
    synchronized (lock) {
      if (retrieved != 0 || replaced != 0 || pruned != 0) {
        Journey.get().debugManager().broadcast(Formatter.debug("[Chunk Cache] Queued: ___, Retrieved: ___, Replaced: ___, Pruned: ___,",
            requestQueue.size(),
            retrieved, replaced, pruned));
        retrieved = 0;
        replaced = 0;
        pruned = 0;
      }
    }
  }

  /**
   * Submits requests to load the chunks with given chunk ids.
   *
   * @param chunkIds the ids of the chunks to load
   */
  public void loadChunks(Collection<ChunkId> chunkIds) {
    synchronized (lock) {
      for (ChunkId chunkId : chunkIds) {
        // Is this chunk already stored in cache?
        JourneyChunk maybeChunk = chunkCache.getChunk(chunkId);
        if (maybeChunk != null) {
          continue;
        }

        // Chunk is not stored in cache. Is it already queued?
        ChunkRequest maybeRequest = requestMap.get(chunkId);
        if (maybeRequest != null) {
          continue;
        }

        // Not stored and not queued. Queue it.
        maybeRequest = new ChunkRequest(chunkId);
        requestQueue.add(maybeRequest);
        requestMap.put(chunkId, maybeRequest);
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
      Future<JourneyChunk> request = null;
      // Request chunks for chunks surrounding the requested one, since they may be wanted later
      int chunkX = chunkId.x();
      int chunkZ = chunkId.z();
      for (int x = chunkX - 1; x <= chunkX + 1; x++) {
        for (int z = chunkZ - 1; z <= chunkZ + 1; z++) {
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
          requestQueue.add(maybeRequest);
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
