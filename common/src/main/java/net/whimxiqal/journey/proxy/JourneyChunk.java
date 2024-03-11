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

package net.whimxiqal.journey.proxy;

import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.chunk.ChunkId;
import net.whimxiqal.journey.search.flag.FlagSet;

/**
 * Journey's representation of a Minecraft chunk.
 * Read-only and thread-safe.
 */
public abstract class JourneyChunk {

  public static final int CHUNK_SIDE_LENGTH = 16;

  private final ChunkId id;

  public JourneyChunk(ChunkId id) {
    this.id = id;
  }

  /**
   * Get a cell from the given= coordinates within this chunk
   *
   * @param x  the x coordinate within the chunk [0-16)
   * @param y  the y coordinate
   * @param z  the z coordinate within the chunk [0-16)
   * @return the cell
   */
  protected final Cell toCell(int x, int y, int z) {
    return new Cell(id.x() * 16 + x, y, id.z() * 16 + z, id.domain());
  }

  /**
   * Get the identifiable parameters for this chunk.
   *
   * @return the id
   */
  public final ChunkId id() {
    return this.id;
  }

  public final JourneyBlock block(int x, int y, int z, FlagSet flagSet) {
    if (y >= 256 || y < -128) {
      return new AirJourneyBlock(toCell(x, y, z));
    }
    return realBlock(x, y, z, flagSet);
  }

  /**
   * Get the block at the given coordinates within the chunk.
   * "Real" signifies that it is within the boundaries of the world
   * within which blocks may be placed/broken etc.
   *
   * @param x       the x coordinate [0-16)
   * @param y       the y coordinate
   * @param z       the z coordinate [0-16)
   * @param flagSet the set of flags that may modify world/block behavior
   * @return the block
   */
  protected abstract JourneyBlock realBlock(int x, int y, int z, FlagSet flagSet);

}
