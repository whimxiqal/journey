package edu.whimc.indicator.spigot.path;

import edu.whimc.indicator.common.path.Completion;
import edu.whimc.indicator.common.path.Link;
import edu.whimc.portals.Portal;
import org.bukkit.World;

public class PortalLink implements Link<LocationCell, World> {

  private final String portalName;
  private final LocationCell origin;
  private final LocationCell destination;
  private final int posX1, posY1, posZ1;
  private final int posX2, posY2, posZ2;
  private final World world;
  private final Completion<LocationCell, World> completion;

  public PortalLink(Portal portal) {
    if (portal.getDestination() == null) {
      throw new IllegalStateException("Error with portal: " + portal.getName()
          + "A Portal Link may only be created with portals that have a destination.");
    }
    if (portal.getDestination().getLocation().getWorld() == null) {
      throw new IllegalStateException("Error with portal: " + portal.getName()
          + ". A Portal Link may only be created with portals that have a world associated with its destination.");
    }
    this.portalName = portal.getName();

    this.posX1 = portal.getPos1().getBlockX();
    this.posY1 = portal.getPos1().getBlockY();
    this.posZ1 = portal.getPos1().getBlockZ();
    this.posX2 = portal.getPos2().getBlockX();
    this.posY2 = portal.getPos2().getBlockY();
    this.posZ2 = portal.getPos2().getBlockZ();
    this.world = portal.getWorld();

    this.origin = getOriginOf(portal);
    this.destination = getDestinationOf(portal);
    this.completion = cell -> Math.min(posX1, posX2) <= cell.getX()
        && cell.getX() <= Math.max(posX1, posX2)
        && Math.min(posY1, posY2) <= cell.getY()
        && cell.getY() <= Math.max(posY1, posY2)
        && Math.min(posZ1, posZ2) <= cell.getZ()
        && cell.getZ() <= Math.max(posZ1, posZ2);
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
    return 2;
  }

  @Override
  public boolean verify() {
    Portal portal = Portal.getPortal(portalName);
    if (portal == null) {
      return false;
    }
    return this.origin.equals(getOriginOf(portal)) && this.destination.equals(getDestinationOf(portal));
  }

  private LocationCell getOriginOf(Portal portal) {
    return new LocationCell((portal.getPos1().getBlockX() + portal.getPos2().getBlockX()) / 2,
        Math.min(portal.getPos1().getBlockY(), portal.getPos2().getBlockY()),  // bottom of portal
        (portal.getPos1().getBlockZ() + portal.getPos2().getBlockZ()) / 2,
        world);
  }

  private LocationCell getDestinationOf(Portal portal) {
    return new LocationCell(portal.getDestination().getLocation());
  }
}
