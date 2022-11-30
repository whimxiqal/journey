/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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

package me.pietelite.journey.common.navigation.journey;

import java.util.Optional;
import java.util.UUID;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.math.Vector;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.ModeType;
import me.pietelite.journey.common.navigation.Moded;
import org.jetbrains.annotations.NotNull;
import static me.pietelite.journey.common.navigation.journey.PlayerJourneySession.CACHED_JOURNEY_STEPS_LENGTH;
import static me.pietelite.journey.common.navigation.journey.PlayerJourneySession.PARTICLE_CYCLE_COUNT;
import static me.pietelite.journey.common.navigation.journey.PlayerJourneySession.PARTICLE_SPAWN_DENSITY;
import static me.pietelite.journey.common.navigation.journey.PlayerJourneySession.PARTICLE_UNIT_DISTANCE;

public class JourneyStep implements Moded {

  private final UUID entityUuid;
  private final String domainId;
  private final Vector start;
  private final Vector path;
  private final Vector unitPath;
  private final double totalLength;
  private final ModeType modeType;
  private boolean done = false;
  private double completedLength = 0;

  public JourneyStep(UUID entityUuid, Cell start, Cell destination, ModeType modeType) {
    if (!start.domainId().equals(destination.domainId())) {
      throw new IllegalArgumentException("The start and destination must be in the same domain");
    }
    this.entityUuid = entityUuid;
    this.domainId = start.domainId();
    this.start = new Vector(start.getX() + 0.5, start.getY() + 0.5, start.getZ() + 0.5);
    this.path = new Vector(destination.getX() - start.getX(), destination.getY() - start.getY(), destination.getZ() - start.getZ());
    this.unitPath = path.unit();
    this.totalLength = path.magnitude();
    this.modeType = modeType;
  }

  /**
   * Update the step with the entity's current location
   * @return the length of the step traversed
   */
  double update() {
    if (done) {
      return 0;
    }
    double startingCompletedLength = completedLength;
    Optional<Vector> maybeLoc = Journey.get().proxy().platform().entityVector(entityUuid);
    if (!maybeLoc.isPresent()) {
      Journey.logger().warn("Could not find location of entity "
          + entityUuid.toString()
          + " while updating JourneyStep");
      done = true;
      return totalLength - startingCompletedLength;
    }

    Vector locRelative = maybeLoc.get().subtract(start);  // location of entity relative to start of step
    completedLength = Math.max(completedLength, locRelative.projectionOnto(path));
    if (completedLength >= totalLength) {
      done = true;
      return totalLength - startingCompletedLength;
    }
    return completedLength - startingCompletedLength;
  }

  boolean done() {
    return done;
  }

  double length() {
    return totalLength;
  }

  void illuminate() {
    if (done) {
      return;
    }
    double distance = completedLength;
    final double startDistance = distance;
    double curX = start.x() + unitPath.x() * distance;
    double curY = start.y() + unitPath.y() * distance;
    double curZ = start.z() + unitPath.z() * distance;
    final double deltaX = unitPath.x() * PARTICLE_UNIT_DISTANCE;
    final double deltaY = unitPath.y() * PARTICLE_UNIT_DISTANCE;
    final double deltaZ = unitPath.z() * PARTICLE_UNIT_DISTANCE;

    // Stop if we reach the end OR the total distance we are displaying is longer than the supposed cached length size (so we're not showing unnecessary particles too far ahead)
    while (distance < totalLength && (distance - startDistance) < CACHED_JOURNEY_STEPS_LENGTH) {
      Journey.get().proxy().platform().spawnModeParticle(entityUuid,
          modeType,
          domainId,
          curX,
          curY,
          curZ,
          PARTICLE_CYCLE_COUNT,
          PARTICLE_SPAWN_DENSITY, PARTICLE_SPAWN_DENSITY, PARTICLE_SPAWN_DENSITY);
      curX += deltaX;
      curY += deltaY;
      curZ += deltaZ;
      distance += PARTICLE_UNIT_DISTANCE;
    }
  }

  @Override
  public @NotNull ModeType modeType() {
    return modeType;
  }

  public String domainId() {
    return domainId;
  }
}
