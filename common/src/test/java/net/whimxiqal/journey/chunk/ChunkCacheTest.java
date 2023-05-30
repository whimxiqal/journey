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
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.JourneyTestHarness;
import net.whimxiqal.journey.search.flag.FlagSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChunkCacheTest extends JourneyTestHarness {

  void correctness(BlockProvider provider, Cell cell) throws ExecutionException, InterruptedException {
    Assertions.assertEquals(cell, provider.toBlock(cell).cell());
  }

  @Test
  void correctness() throws ExecutionException, InterruptedException {
    ChunkCacheBlockProvider chunkCache = new ChunkCacheBlockProvider(10, new FlagSet());

    correctness(chunkCache, new Cell(0, 0, 0, 0));
    correctness(chunkCache, new Cell(1, 2, 3, 4));
    correctness(chunkCache, new Cell(-1, -3, -5, 2));
    correctness(chunkCache, new Cell(101, -202, -303, 404));
    correctness(chunkCache, new Cell(-16, 0, -16, 1)); // on the edge of the chunk
  }

}