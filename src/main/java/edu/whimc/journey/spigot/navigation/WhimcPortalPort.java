package edu.whimc.journey.spigot.navigation;

import edu.whimc.journey.common.navigation.Port;
import edu.whimc.journey.common.navigation.ModeType;
import edu.whimc.journey.common.tools.Verifiable;
import edu.whimc.journey.spigot.util.SpigotUtil;
import edu.whimc.portals.Portal;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public class WhimcPortalPort extends Port<LocationCell, World> implements Verifiable {

  private final String portalName;

  public static WhimcPortalPort from(Portal portal) {
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

    LocationCell origin = getOriginOf(portal);
    if (origin == null) {
      throw new IllegalStateException("Error with portal: " + portal.getName()
          + ". A reachable central location could not be identified.");
    }

    LocationCell destination = getDestinationOf(portal);

    return new WhimcPortalPort(portal.getName(), origin, destination);
  }

  private WhimcPortalPort(String name, LocationCell origin, LocationCell destination) {
    super(origin, destination, ModeType.LEAP, 5);
    this.portalName = name;
    World world = origin.getDomain();
  }

  @Override
  public boolean verify() {
    Portal portal = Portal.getPortal(portalName);
    if (portal == null) {
      return false;
    }
    return getOrigin().equals(getOriginOf(portal)) && getDestination().equals(getDestinationOf(portal));
  }

  @Nullable
  private static LocationCell getOriginOf(Portal portal) {
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
                return new LocationCell(xLoc, yLoc, zLoc, portal.getWorld());
              }
            }
          }
        }
        // Nothing at all found
        return null;
      }
    }
    // We found one at the center of the portal!
    return new LocationCell(xLoc, yLoc, zLoc, portal.getWorld());
  }

  private static LocationCell getDestinationOf(Portal portal) {
    return new LocationCell(portal.getDestination().getLocation());
  }

  @Override
  public String toString() {
    return "PortalLink{portalName='" + portalName + "'}";
  }

  @Override
  public boolean completeWith(LocationCell location) {
    Portal portal = Portal.getPortal(this.portalName);
    return Math.min(portal.getPos1().getBlockX(), portal.getPos2().getBlockX()) <= location.getX()
        && location.getX() <= Math.max(portal.getPos1().getBlockX(), portal.getPos2().getBlockX())
        && Math.min(portal.getPos1().getBlockY(), portal.getPos2().getBlockY()) <= location.getY()
        && location.getY() <= Math.max(portal.getPos1().getBlockY(), portal.getPos2().getBlockY())
        && Math.min(portal.getPos1().getBlockZ(), portal.getPos2().getBlockZ()) <= location.getZ()
        && location.getZ() <= Math.max(portal.getPos1().getBlockZ(), portal.getPos2().getBlockZ());
  }

}
