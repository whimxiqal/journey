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

package net.whimxiqal.journey.navigation.journey;

import java.util.Optional;
import java.util.UUID;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.math.Vector;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.navigation.ModeType;
import net.whimxiqal.journey.navigation.Moded;
import org.jetbrains.annotations.NotNull;

public class JourneyStep implements Moded {

  private final UUID entityUuid;
  private final String domainId;
  private final Vector start;
  private final Vector path;
  private final Vector unitPath;
  private final double totalLength;
  private final ModeType modeType;
  private final Cell destination;
  private boolean done = false;
  private double completedLength = 0;

  public JourneyStep(UUID entityUuid, Cell start, Cell destination, ModeType modeType) {
    if (!start.domainId().equals(destination.domainId())) {
      throw new IllegalArgumentException("The start and destination must be in the same domain");
    }
    this.entityUuid = entityUuid;
    this.domainId = start.domainId();
    this.start = new Vector(start.blockX() + 0.5, start.blockY() + 0.5, start.blockZ() + 0.5);
    this.path = new Vector(destination.blockX() - start.blockX(), destination.blockY() - start.blockY(), destination.blockZ() - start.blockZ());
    this.unitPath = path.unit();
    this.totalLength = path.magnitude();
    this.modeType = modeType;
    this.destination = destination;
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
      Journey.logger().error("Could not find location of entity "
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
    final double deltaX = unitPath.x() * PlayerJourneySession.PARTICLE_UNIT_DISTANCE;
    final double deltaY = unitPath.y() * PlayerJourneySession.PARTICLE_UNIT_DISTANCE;
    final double deltaZ = unitPath.z() * PlayerJourneySession.PARTICLE_UNIT_DISTANCE;

    // Stop if we reach the end OR the total distance we are displaying is longer than the supposed cached length size (so we're not showing unnecessary particles too far ahead)
    while (distance < totalLength && (distance - startDistance) < PlayerJourneySession.CACHED_JOURNEY_STEPS_LENGTH) {
      Journey.get().proxy().platform().spawnModeParticle(entityUuid,
          modeType,
          domainId,
          curX,
          curY,
          curZ,
          PlayerJourneySession.PARTICLE_CYCLE_COUNT,
          PlayerJourneySession.PARTICLE_SPAWN_DENSITY, PlayerJourneySession.PARTICLE_SPAWN_DENSITY, PlayerJourneySession.PARTICLE_SPAWN_DENSITY);
      curX += deltaX;
      curY += deltaY;
      curZ += deltaZ;
      distance += PlayerJourneySession.PARTICLE_UNIT_DISTANCE;
    }
  }

  @Override
  public @NotNull ModeType modeType() {
    return modeType;
  }

  public String domainId() {
    return domainId;
  }

  public Cell destination() {
    return destination;
  }
}
