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
