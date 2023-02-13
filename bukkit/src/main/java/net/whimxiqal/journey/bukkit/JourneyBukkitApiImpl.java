package net.whimxiqal.journey.bukkit;

import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import org.bukkit.Location;
import org.bukkit.World;

public class JourneyBukkitApiImpl implements JourneyBukkitApi {
  @Override
  public String toDomainId(World world) {
    return BukkitUtil.getWorldId(world);
  }

  @Override
  public Cell toCell(Location location) {
    return BukkitUtil.cell(location);
  }
}
