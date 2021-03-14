package edu.whimc.indicator.spigot.util;

import edu.whimc.indicator.spigot.path.LocationCell;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.*;

public final class NetherUtil {

  private NetherUtil() {
  }

  private static final BlockFace[] BLOCK_FACES = new BlockFace[]{
      BlockFace.EAST,
      BlockFace.WEST,
      BlockFace.NORTH,
      BlockFace.SOUTH,
      BlockFace.UP,
      BlockFace.DOWN};

  /**
   * * Gets the nearest Nether portal within the specified radius in relation to the given cell.
   *
   * @param origin    Center of the search radius
   * @param radius    The search radius
   * @param minHeight Minimum height of search
   * @param maxHeight Maximum height of search
   * @return Returns cell in the bottom center of the nearest nether portal if found. Otherwise returns null.
   */
  public static Collection<PortalGroup> locateAll(LocationCell origin, int radius, int minHeight, int maxHeight) {
    Set<PortalGroup> portals = new HashSet<>();  // All PortalGroups found
    Set<LocationCell> stored = new HashSet<>();  // All Portal blocks found in the PortalGroups
    World world = origin.getDomain();

    int yStart = Math.max(origin.getY() - radius, minHeight);
    int yEnd = Math.min(origin.getY() + radius, maxHeight);

    for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
      for (int y = yStart; y <= yEnd; y += 2) {
        for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
          if (x + z % 2 == 0) break;  // Check only in checkerboard pattern
          //Location being iterated over.
          LocationCell cell = new LocationCell(x, y, z, world);
          //Don't do anything if the Portal block is already stored.
          if (!stored.contains(cell)) {
            PortalGroup pg = getPortalBlocks(cell);
            //Do nothing if there are no Portal blocks
            if (pg != null) {
              //If the PortalGroup was added, store the Portal blocks in the Collection
              if (portals.add(pg)) {
                stored.addAll(pg.getBlocks());

              }
            }
          }
        }
      }
    }
    return portals;
  }

  public static Collection<PortalGroup> locateAll(LocationCell cell, int radius) {
    World world = cell.getDomain();
    int maxHeight;
    if (world.getEnvironment().equals(World.Environment.NORMAL)) {
      maxHeight = world.getMaxHeight() - 1;
    } else if (world.getEnvironment().equals(World.Environment.NETHER)) {
      maxHeight = 127;
    } else {
      throw new IllegalArgumentException("The world environment must either be Normal or Nether");
    }
    return locateAll(cell, radius, 1, maxHeight);
  }

  public static Optional<PortalGroup> locateMatch(LocationCell cell) {
    World overworld = Bukkit.getWorlds().stream()
        .filter(w -> w.getEnvironment().equals(World.Environment.NORMAL))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Could not find an overworld"));
    World nether = Bukkit.getWorlds().stream()
        .filter(w -> w.getEnvironment().equals(World.Environment.NETHER))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Could not find a nether world"));
    World world = cell.getDomain();
    LocationCell corresponding;
    int radius;
    if (world.getEnvironment().equals(World.Environment.NORMAL)) {
      corresponding = new LocationCell(cell.getX() / 8, cell.getY(), cell.getZ() / 8, nether);
      radius = 1024;
    } else if (world.getEnvironment().equals(World.Environment.NETHER)) {
      corresponding = new LocationCell(cell.getX() * 8, cell.getY(), cell.getZ() * 8, overworld);
      radius = 128;
    } else {
      throw new IllegalArgumentException("The world environment must either be Normal or Nether");
    }
    return locateAll(corresponding, radius)
        .stream()
        .min(Comparator.comparingDouble(group -> group.port().distanceToSquared(corresponding)));
  }

  /**
   * Gets the Portal blocks that is part of the Nether portal.
   *
   * @param cell - Location to start the getting the portal blocks.
   * @return A PortalGroup of all the found Portal blocks. Otherwise returns null. This will return null if the amount of found blocks if below 6.
   */
  public static PortalGroup getPortalBlocks(LocationCell cell) {
    if (cell.getBlock().getType() != Material.NETHER_PORTAL)
      return null;

    PortalGroup group = portalBlock(new PortalGroup(cell.getDomain()), cell);
    return group.size() > 5 ? group : null;
  }

  private static PortalGroup portalBlock(PortalGroup group, LocationCell cell) {
    for (BlockFace face : BLOCK_FACES) {
      Block relative = cell.getBlock().getRelative(face);
      LocationCell relLoc = new LocationCell(relative.getLocation());
      if (group.add(relLoc)) {
        portalBlock(group, relLoc);
      }
    }
    return group;
  }

  public static class PortalGroup {
    private final Set<LocationCell> portal = new HashSet<>();
    private final HashMap<Integer, Set<LocationCell>> yBlock = new HashMap<>();
    private final World world;
    private LocationCell teleportTo;
    private int bottom = Integer.MAX_VALUE;

    /**
     * A group of Portal block for a Nether portal.
     *
     * @param world The world the Portal blocks resides in.
     */
    public PortalGroup(World world) {
      this.world = world;
    }

    /**
     * Adds the Location to the PortalGroup.
     *
     * @param cell Vector to add
     * @return if the Location was added. Otherwise false.
     */
    public boolean add(LocationCell cell) {
      // Check to see if the block is a Portal block.
      if (cell.getBlock().getType() != Material.NETHER_PORTAL) return false;

      boolean added = portal.add(cell);
      // If the cell was added, do more actions.
      if (added) {
        int y = cell.getY();
        if (y < bottom) {
          //The bottom of the Nether portal
          bottom = cell.getY();
        }
        //Put the Location in a Map sorted by Y value.
        Set<LocationCell> set = yBlock.computeIfAbsent(y, k -> new HashSet<>());
        set.add(cell);
        //Reset the teleport cell and distance squared since a new block was added.
        teleportTo = null;
      }
      return added;
    }

    public int size() {
      return portal.size();
    }

    public Collection<LocationCell> getBlocks() {
      return Collections.unmodifiableCollection(portal);
    }

    /**
     * Gets the place to teleport an entity to the Nether portal.
     *
     * @return The cell at the bottom center of the Nether portal.
     */
    public LocationCell port() {
      if (teleportTo != null)
        return teleportTo;

      if (portal.size() == 0)
        return null;

      Set<LocationCell> bottomY = yBlock.get(bottom);
      int xMin = Collections.min(bottomY, Comparator.comparingInt(LocationCell::getX)).getX();
      int xMax = Collections.max(bottomY, Comparator.comparingInt(LocationCell::getX)).getX();
      int zMin = Collections.min(bottomY, Comparator.comparingInt(LocationCell::getZ)).getZ();
      int zMax = Collections.max(bottomY, Comparator.comparingInt(LocationCell::getZ)).getZ();

      return new LocationCell((xMax + xMin) / 2, bottom, (zMax + zMin) / 2, world);
    }

    @Override
    public int hashCode() {
      return portal.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof PortalGroup) {
        PortalGroup pg = (PortalGroup) o;
        return portal.equals(pg.portal);
      }
      return portal.equals(o);
    }

  }
}