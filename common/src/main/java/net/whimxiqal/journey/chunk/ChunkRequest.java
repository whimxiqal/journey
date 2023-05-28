package net.whimxiqal.journey.chunk;

import java.util.concurrent.CompletableFuture;
import net.whimxiqal.journey.proxy.JourneyChunk;

/**
 * A container for a request for a {@link JourneyChunk}.
 * It contains the id of the chunk and a future to be completed with the retrieved chunk.
 */
class ChunkRequest {

  private final ChunkId chunkId;
  private final CompletableFuture<JourneyChunk> future = new CompletableFuture<>();

  ChunkRequest(ChunkId chunkId) {
    this.chunkId = chunkId;
  }

  public ChunkId chunkId() {
    return chunkId;
  }

  public CompletableFuture<JourneyChunk> future() {
    return future;
  }

  @Override
  public String toString() {
    return "ChunkRequest{" +
        "chunkId=" + chunkId +
        '}';
  }

}
