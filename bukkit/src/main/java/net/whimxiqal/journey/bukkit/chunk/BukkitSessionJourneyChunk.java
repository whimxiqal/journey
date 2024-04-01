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

package net.whimxiqal.journey.bukkit.chunk;

import java.util.UUID;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.chunk.ChunkId;
import net.whimxiqal.journey.proxy.JourneyBlock;
import net.whimxiqal.journey.proxy.JourneyChunk;
import net.whimxiqal.journey.search.flag.FlagSet;
import org.bukkit.ChunkSnapshot;

public class BukkitSessionJourneyChunk extends JourneyChunk {

  private final ChunkSnapshot chunk;

  public BukkitSessionJourneyChunk(ChunkSnapshot chunk, UUID worldUuid) {
    super(new ChunkId(Journey.get().domainManager().domainIndex(worldUuid), chunk.getX(), chunk.getZ()));
    this.chunk = chunk;
  }

  @Override
  public JourneyBlock realBlock(int x, int y, int z, FlagSet flagSet) {
    return new BukkitSessionJourneyBlock(toCell(x, y, z), chunk.getBlockData(x, y, z), chunk.getBlockData(x, y - 1, z), flagSet);
  }
}
