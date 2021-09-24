package edu.whimc.indicator.spigot.navigation;

import edu.whimc.indicator.common.navigation.Completion;
import edu.whimc.indicator.common.navigation.Link;
import edu.whimc.indicator.spigot.util.SpigotUtil;
import edu.whimc.portals.Portal;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public class PortalLink implements Link<LocationCell, World> {

  private final String portalName;
  private final LocationCell origin;
  private final LocationCell destination;
  private final World world;
  private final Completion<LocationCell, World> completion;

  public PortalLink(Portal portal) {
    if (portal.getWorld() == null) {
      throw new IllegalStateException("Error with portal: " + portal.getName()
          + "A Portal Link may only be created with portals that have a world.");
    }
    if (portal.getDestination() == null) {
      throw new IllegalStateException("Error with portal: " + portal.getName()
          + "A Portal Link may only be created with portals that have a destination.");
    }
    if (portal.getDestination().getLocation().getWorld() == null) {
      throw new IllegalStateException("Error with portal: " + portal.getName()
          + ". A Portal Link may only be created with portals that have a world associated with its destination.");
    }
    this.portalName = portal.getName();
    this.world = portal.getWorld();

    this.origin = getOriginOf(portal);
    if (this.origin == null) {
      throw new IllegalStateException("Error with portal: " + portal.getName()
          + ". A reachable central location could not be identified.");
    }
    this.destination = getDestinationOf(portal);
    this.completion = cell -> Math.min(portal.getPos1().getBlockX(), portal.getPos2().getBlockX()) <= cell.getX()
        && cell.getX() <= Math.max(portal.getPos1().getBlockX(), portal.getPos2().getBlockX())
        && Math.min(portal.getPos1().getBlockY(), portal.getPos2().getBlockY()) <= cell.getY()
        && cell.getY() <= Math.max(portal.getPos1().getBlockY(), portal.getPos2().getBlockY())
        && Math.min(portal.getPos1().getBlockZ(), portal.getPos2().getBlockZ()) <= cell.getZ()
        && cell.getZ() <= Math.max(portal.getPos1().getBlockZ(), portal.getPos2().getBlockZ());
  }

  @Override
  public LocationCell getOrigin() {
    return origin;
  }

  @Override
  public LocationCell getDestination() {
    return destination;
  }

  @Override
  public Completion<LocationCell, World> getCompletion() {
    return completion;
  }

  @Override
  public double weight() {
    return 3;
  }

  @Override
  public boolean verify() {
    Portal portal = Portal.getPortal(portalName);
    if (portal == null) {
      return false;
    }
    return this.origin.equals(getOriginOf(portal)) && this.destination.equals(getDestinationOf(portal));
  }

  @Nullable
  private LocationCell getOriginOf(Portal portal) {
    // Start by trying to use the center of the portal.
    int xLoc = (portal.getPos1().getBlockX() + portal.getPos2().getBlockX()) / 2;  // center of portal
    int yLoc = Math.min(portal.getPos1().getBlockY(), portal.getPos2().getBlockY());  // bottom of portal
    int zLoc = (portal.getPos1().getBlockZ() + portal.getPos2().getBlockZ()) / 2;
    while (!SpigotUtil.isLaterallyPassable(portal.getWorld().getBlockAt(xLoc, yLoc, zLoc))
        || !SpigotUtil.isPassable(portal.getWorld().getBlockAt(xLoc, yLoc + 1, zLoc))) {
      yLoc++;
      if (yLoc > Math.max(portal.getPos1().getBlockY(), portal.getPos2().getBlockY())) {
        // There is no y value that works for the center of this portal.
        // Try every other point and see what sticks (this does not repeat)
        for (xLoc = portal.getPos1().getBlockX(); xLoc <= portal.getPos2().getBlockX(); xLoc++) {
          for (yLoc = portal.getPos1().getBlockY(); yLoc < portal.getPos2().getBlockY(); yLoc++) {
            for (zLoc = portal.getPos1().getBlockZ(); zLoc <= portal.getPos2().getBlockZ(); zLoc++) {
              if (SpigotUtil.isLaterallyPassable(portal.getWorld().getBlockAt(xLoc, yLoc, zLoc))
              && SpigotUtil.isPassable(portal.getWorld().getBlockAt(xLoc, yLoc + 1, zLoc))) {
                return new LocationCell(xLoc, yLoc, zLoc, world);
              }
            }
          }
        }
        // Nothing at all found
        return null;
      }
    }
    // We found one at the center of the portal!
    return new LocationCell(xLoc, yLoc, zLoc, world);
  }

  private LocationCell getDestinationOf(Portal portal) {
    return new LocationCell(portal.getDestination().getLocation());
  }

  @Override
  public String toString() {
    return "PortalLink{portalName='" + portalName + "'}";
  }
}
