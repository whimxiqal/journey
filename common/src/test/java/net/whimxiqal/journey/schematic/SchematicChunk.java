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
