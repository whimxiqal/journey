package net.whimxiqal.journey.proxy;

import net.whimxiqal.journey.chunk.ChunkId;
import net.whimxiqal.journey.search.flag.FlagSet;

/**
 * Journey's representation of a Minecraft chunk.
 */
public interface JourneyChunk {

  /**
   * Get the identifiable parameters for this chunk.
   *
   * @return the id
   */
  ChunkId id();

  /**
   * Get the block at the given coordinates within the chunk.
   *
   * @param x the x coordinate [0-16)
   * @param y the y coordinate
   * @param z the z coordinate [0-16)
   * @param flagSet the set of flags that may modify world/block behavior
   * @return the block
   */
  JourneyBlock block(int x, int y, int z, FlagSet flagSet);

}
