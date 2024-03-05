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

package net.whimxiqal.journey.sponge.chunk;

import net.whimxiqal.journey.chunk.ChunkId;
import net.whimxiqal.journey.proxy.JourneyBlock;
import net.whimxiqal.journey.proxy.JourneyChunk;
import net.whimxiqal.journey.search.flag.FlagSet;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.chunk.WorldChunk;

public class SpongeSessionJourneyChunk implements JourneyChunk {

  private final ChunkId id;
  private final BlockState[][][] chunk;

  public SpongeSessionJourneyChunk(ChunkId id, WorldChunk chunk) {
    this.id = id;

    // TODO: this is gross and should use the block volume API, but it seems broken
    this.chunk = new BlockState[16][384][16];
    for (int x = 0; x < 16; x++) {
      for (int y = 0; y < 384; y++) {
        for (int z = 0; z < 16; z++) {
          this.chunk[x][y][z] = chunk.block(chunk.min().add(x, y - 128, z)).copy();
        }
      }
    }
  }

  @Override
  public ChunkId id() {
    return id;
  }

  @Override
  public JourneyBlock block(int x, int y, int z, FlagSet flagSet) {
    return new SpongeSessionJourneyBlock(JourneyChunk.toCell(id, x, y, z),
        y >= 256 || y < -128 ? BlockTypes.AIR.get().defaultState() : chunk[x][y + 128][z],
        y >= 256 || y <= -128 ? BlockTypes.AIR.get().defaultState() : chunk[x][y + 128 - 1][z],
        flagSet);
  }
}
