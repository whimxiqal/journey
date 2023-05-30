/*
 * MIT License
 *
 * Copyright (c) whimxiqal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.whimxiqal.journey.bukkit.navigation.mode;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import net.whimxiqal.journey.chunk.BlockProvider;
import net.whimxiqal.journey.navigation.Mode;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public abstract class RayTraceMode extends Mode {

  private final static long CALCULATION_COOLDOWN_MS = 1000;  // once per second;
  private final static double MAX_DISTANCE = 1024;
  private final static double MIN_VIABLE_DISTANCE_SQUARED = 10 * 10;  // anything smaller than this distance will discount this entire mode per check
  private final static double RAY_TRACE_MAX_SEPARATION = 0.2;  // number of blocks between each ray trace
  private final int domain;
  private final Cell destinationCell;
  private final Location destination;
  private final double crossSectionLengthX;   // length x of bounding box (player or vehicle)
  private final double crossSectionLengthY;   // length y of bounding box (player or vehicle)
  private final double crossSectionLengthZ;   // length z of bounding box (player or vehicle)
  private final FluidCollisionMode fluidCollisionMode;
  private long lastCalculationTime = System.currentTimeMillis();

  public RayTraceMode(Cell destination,
                      double crossSectionLengthX, double crossSectionLengthY, double crossSectionLengthZ,
                      FluidCollisionMode fluidCollisionMode) {
    this.domain = destination.domain();
    this.destinationCell = destination;
    this.destination = BukkitUtil.toLocation(destination);
    this.crossSectionLengthX = crossSectionLengthX;
    this.crossSectionLengthY = crossSectionLengthY;
    this.crossSectionLengthZ = crossSectionLengthZ;
    this.fluidCollisionMode = fluidCollisionMode;
  }

  @Override
  public List<Option> getDestinations(@NotNull Cell origin, BlockProvider blockProvider) throws ExecutionException, InterruptedException {
    List<Option> options = new LinkedList<>();
    if (origin.domain() != domain) {
      return options;  // this can only be used when we're in the correct world
    }
    if (origin.equals(destinationCell)) {
      return options;
    }

    long now = System.currentTimeMillis();
    if (lastCalculationTime + CALCULATION_COOLDOWN_MS > now) {
      // don't attempt the calculation yet, we're still cooling down
      return options;
    }
    if (!shouldAttemptCalculation(origin)) {
      return options;
    }

    // attempt calculation
    lastCalculationTime = now;

    AtomicReference<Cell> result = new AtomicReference<>(null);
    BukkitUtil.runSync(() -> {
      final World world = BukkitUtil.getWorld(origin);
      final Location originLocation = BukkitUtil.toLocation(origin);
      final Vector originVector = originLocation.toVector();
      final Vector destinationVector = destination.toVector();
      final double totalDistance = destinationVector.distance(originVector);
      final Vector direction = direction(originVector, destinationVector);

      final double halfCrossSectionalLengthX = crossSectionLengthX / 2;
      final double halfCrossSectionalLengthZ = crossSectionLengthZ / 2;

      final double startX = origin.blockX() + 0.5;
      final double startY = origin.blockY();
      final double startZ = origin.blockZ() + 0.5;

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
        result.set(BukkitUtil.cell(destination));
      } else {
        assert worstTrace != null;
        result.set(BukkitUtil.cell(worstTrace.getHitBlock()
            .getLocation()
            .add(worstTrace.getHitBlockFace().getDirection())
            .subtract(0, -Math.floor(worstTraceYOffset), 0)));
      }
    });
    if (result.get() == null) {
      return options;
    }
    if (result.get().equals(origin)) {
      return options;
    }
    if (!blockProvider.toBlock(result.get()).isPassable() || !blockProvider.toBlock(result.get().atOffset(0, 1, 0)).isPassable()) {
      return options;  // we can't stand here!
    }

    completeWith(origin, result.get(), options);

    return options;
  }

  protected Vector direction(Vector origin, Vector destination) {
    return destination.subtract(origin);
  }

  protected abstract boolean shouldAttemptCalculation(Cell origin);

  protected abstract void completeWith(Cell origin, Cell destination, List<Option> options);

  private RayTraceResult rayTraceSingle(Location location, Vector direction, double totalDistance) {
    return Objects.requireNonNull(location.getWorld()).rayTraceBlocks(location,
        direction,
        Math.min(MAX_DISTANCE, totalDistance),
        fluidCollisionMode,
        false);
  }

}
