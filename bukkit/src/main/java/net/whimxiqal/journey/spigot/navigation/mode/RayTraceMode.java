package net.whimxiqal.journey.spigot.navigation.mode;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.journey.common.search.SearchSession;
import net.whimxiqal.journey.spigot.util.SpigotUtil;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public abstract class RayTraceMode extends SpigotMode {

  private final static double MAX_DISTANCE = 1024;
  private final static double MIN_VIABLE_DISTANCE_SQUARED = 10 * 10;  // anything smaller than this distance will discount this entire mode per check
  private final static double RAY_TRACE_MAX_SEPARATION = 0.2;  // number of blocks between each ray trace
  private final String domainId;
  private final Cell destinationCell;
  private final Location destination;
  private final double crossSectionLengthX;   // length x of bounding box (player or vehicle)
  private final double crossSectionLengthY;   // length y of bounding box (player or vehicle)
  private final double crossSectionLengthZ;   // length z of bounding box (player or vehicle)
  private final FluidCollisionMode fluidCollisionMode;

  public RayTraceMode(SearchSession session, Set<Material> forcePassable, Cell destination,
                      double crossSectionLengthX, double crossSectionLengthY, double crossSectionLengthZ,
                      FluidCollisionMode fluidCollisionMode) {
    super(session, forcePassable);
    this.domainId = destination.domainId();
    this.destinationCell = destination;
    this.destination = SpigotUtil.toLocation(destination);
    this.crossSectionLengthX = crossSectionLengthX;
    this.crossSectionLengthY = crossSectionLengthY;
    this.crossSectionLengthZ = crossSectionLengthZ;
    this.fluidCollisionMode = fluidCollisionMode;
  }

  @Override
  protected void collectDestinations(@NotNull Cell origin, @NotNull List<Option> options) {
    if (!origin.domainId().equals(domainId)) {
      return;  // this can only be used when we're in the correct world
    }
    if (origin.equals(destinationCell)) {
      return;
    }
    if (!check(origin)) {
      return;
    }
    final World world = SpigotUtil.getWorld(origin);
    final Location originLocation = SpigotUtil.toLocation(origin);
    final Vector originVector = originLocation.toVector();
    final Vector destinationVector = destination.toVector();
    final double totalDistance = destinationVector.distance(originVector);
    final Vector direction = direction(originVector, destinationVector);

    final double halfCrossSectionalLengthX = crossSectionLengthX / 2;
    final double halfCrossSectionalLengthZ = crossSectionLengthZ / 2;

    final double startX = origin.getX() + 0.5;
    final double startY = origin.getY();
    final double startZ = origin.getZ() + 0.5;

    AtomicReference<Cell> result = new AtomicReference<>(null);
    SpigotUtil.runSync(() -> {
      RayTraceResult trace;
      double distanceSquared;
      double worstDistanceSquared = Double.MAX_VALUE;
      RayTraceResult worstTrace = null;
      double worstTraceYOffset = 0;
      boolean canGoDirect = true;
      for (double x = startX - halfCrossSectionalLengthX; x < startX + halfCrossSectionalLengthX + RAY_TRACE_MAX_SEPARATION; x += RAY_TRACE_MAX_SEPARATION) {
        for (double y = startY; y < startY + crossSectionLengthY + RAY_TRACE_MAX_SEPARATION; y += RAY_TRACE_MAX_SEPARATION) {
          for (double z = startZ - halfCrossSectionalLengthZ; z < startZ + halfCrossSectionalLengthZ + RAY_TRACE_MAX_SEPARATION; z += RAY_TRACE_MAX_SEPARATION) {
            trace = rayTraceSingle(new Location(world, x, y, z), direction, totalDistance);
            if (trace == null) {
              // no hit -- we can go directly to the destination!
              continue;
            }
            canGoDirect = false;
            distanceSquared = trace.getHitPosition().distanceSquared(originVector);
            if (distanceSquared < MIN_VIABLE_DISTANCE_SQUARED) {
              return;
            }
            if (distanceSquared > worstDistanceSquared) {
              continue;
            }
            worstDistanceSquared = distanceSquared;
            worstTrace = trace;
            worstTraceYOffset = y - startY;
          }
        }
      }
      if (canGoDirect) {
        result.set(SpigotUtil.cell(destination));
      } else {
        assert worstTrace != null;
        result.set(SpigotUtil.cell(worstTrace.getHitBlock()
            .getLocation()
            .add(worstTrace.getHitBlockFace().getDirection())
            .subtract(0, -Math.floor(worstTraceYOffset), 0)));
      }
    });
    if (result.get() == null) {
      return;
    }
    if (result.get().equals(origin)) {
      return;
    }
    if (!isPassable(SpigotUtil.getBlock(result.get())) || !isPassable(SpigotUtil.getBlock(result.get().atOffset(0, 1, 0)))) {
      return;  // we can't stand here!
    }
    finish(origin, result.get(), options);
  }

  protected Vector direction(Vector origin, Vector destination) {
    return destination.subtract(origin);
  }

  protected abstract boolean check(Cell origin);

  protected abstract void finish(Cell origin, Cell destination, List<Option> options);

  private RayTraceResult rayTraceSingle(Location location, Vector direction, double totalDistance) {
    return Objects.requireNonNull(location.getWorld()).rayTraceBlocks(location,
        direction,
        Math.min(MAX_DISTANCE, totalDistance),
        fluidCollisionMode,
        false);
  }

}
