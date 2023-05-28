package net.whimxiqal.journey.bukkit.chunk;

import java.util.UUID;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.chunk.ChunkId;
import net.whimxiqal.journey.proxy.JourneyBlock;
import net.whimxiqal.journey.proxy.JourneyChunk;
import net.whimxiqal.journey.search.flag.FlagSet;
import org.bukkit.ChunkSnapshot;

public class BukkitSessionJourneyChunk implements JourneyChunk {

  private final ChunkId id;
  private final ChunkSnapshot chunk;

  public BukkitSessionJourneyChunk(ChunkSnapshot chunk, UUID worldUuid) {
    this.id = new ChunkId(Journey.get().domainManager().domainIndex(worldUuid), chunk.getX(), chunk.getZ());
    this.chunk = chunk;
  }

  @Override
  public ChunkId id() {
    return id;
  }

  @Override
  public JourneyBlock block(int x, int y, int z, FlagSet flagSet) {
    return new BukkitSessionJourneyBlock(new Cell(id.x() * 16 + x, y, id.z() * 16 + z, id.domain()), chunk.getBlockData(x, y, z), flagSet);
  }
}
