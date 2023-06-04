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

package net.whimxiqal.journey.schematic;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.chunk.ChunkId;
import net.whimxiqal.journey.proxy.JourneyBlock;
import net.whimxiqal.journey.proxy.JourneyChunk;
import net.whimxiqal.journey.search.flag.FlagSet;

public record SchematicChunk(ChunkId chunkId, Clipboard clipboard) implements JourneyChunk {

  @Override
  public ChunkId id() {
    return chunkId;
  }

  @Override
  public JourneyBlock block(int x, int y, int z, FlagSet flagSet) {
    Cell cell = new Cell(chunkId.x() * CHUNK_SIDE_LENGTH + x, y, chunkId.z() * CHUNK_SIDE_LENGTH + z, 0);
    return new SchematicBlock(cell, clipboard.getBlock(BlockVector3.at(cell.blockX(), cell.blockY(), cell.blockZ())).getBlockType());
  }
}
