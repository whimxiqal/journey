package edu.whimc.indicator.spigot.path;

import edu.whimc.indicator.api.path.Completion;
import edu.whimc.indicator.api.path.Link;
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
    this.portalName = portal.getName();

    this.posX1 = portal.getPos1().getBlockX();
    this.posY1 = portal.getPos1().getBlockY();
    this.posZ1 = portal.getPos1().getBlockZ();
    this.posX2 = portal.getPos2().getBlockX();
    this.posY2 = portal.getPos2().getBlockY();
    this.posZ2 = portal.getPos2().getBlockZ();
    this.world = portal.getWorld();

    this.origin = new LocationCell((posX1 + posX2) / 2,
        Math.min(posY1, posY2),  // bottom of portal
        (posZ1 + posZ2) / 2,
        world);
    this.destination = new LocationCell(portal.getDestination().getLocation());
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
    return Portal.getPortal(portalName) != null;
  }
}
