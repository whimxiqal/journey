package net.whimxiqal.journey.bukkit;

import net.whimxiqal.journey.Cell;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * The interface for Bukkit-specific external-facing endpoints for Journey.
 */
public interface JourneyBukkitApi {

  /**
   * Convert the {@link World} to a domain identifier, which is used to identify
   * worlds in Journey.
   *
   * @param world the Bukkit world
   * @return the domain id
   */
  String toDomainId(World world);

  /**
   * Convert a {@link Location} to a {@link Cell}, which is just Journey's version of a location.
   *
   * @param location the location
   * @return the cell
   */
  Cell toCell(Location location);

}
