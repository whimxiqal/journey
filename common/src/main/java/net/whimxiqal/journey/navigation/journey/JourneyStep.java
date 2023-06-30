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

package net.whimxiqal.journey.navigation.journey;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.math.Vector;

public class JourneyStep {

  private static final double PARTICLE_UNIT_DISTANCE = 0.5;  // number of blocks between which particles will be shown
  private static final double PI_TIMES_2 = Math.PI * 2;
  private static final double TRAIL_DENSITY_FACTOR = 0.1;  // arbitrary factor to tune how it actually looks in game
  private static final Vector RANDOM_VECTOR_1 = new Vector(1, 0, 0);
  private static final Vector RANDOM_VECTOR_2 = new Vector(0, 1, 0);
  private final Random random = new Random();
  private final UUID entityUuid;
  private final int domain;
  private final Vector start;
  private final Vector path;
  private final Vector unitPath;
  private final Vector orthogonalUnit1;  // unit vector orthogonal to unit path, to form a basis
  private final Vector orthogonalUnit2;  // another unit vector orthogonal to unit path, to form a basis
  private final double totalLength;
  private final String particleType;
  private final double crossSectionRadius;
  private final double countPerCycle;  // average number of particles to spawn per cycle
  private final double countCeil;  // ceil of average number of particles to spawn, for looping calculation
  private final double particleProbability;  // probability for each particle to spawn within each loop, to achieve correct average
  private final Cell destination;
  private boolean done = false;
  private double completedLength = 0;

  public JourneyStep(UUID entityUuid, Cell start, Cell destination, String particleType, double width, double density) {
    if (start.domain() != destination.domain()) {
      throw new IllegalArgumentException("The start and destination must be in the same domain");
    }
    this.entityUuid = entityUuid;
    this.domain = start.domain();
    this.start = new Vector(start.blockX() + 0.5, start.blockY() + 1, start.blockZ() + 0.5);
    this.path = new Vector(destination.blockX() - start.blockX(), destination.blockY() - start.blockY(), destination.blockZ() - start.blockZ());
    this.unitPath = path.unit();
    this.totalLength = path.magnitude();
    this.particleType = particleType;
    this.crossSectionRadius = width / 2;
    // density -> count calculation assumes we're making prisms, but we're actually making cylinders.
    // Doesn't really matter, it's a constant conversion and the units are arbitrary anyway
    this.countPerCycle = density * width * width * PARTICLE_UNIT_DISTANCE * TRAIL_DENSITY_FACTOR;
    this.countCeil = Math.ceil(countPerCycle);
    this.particleProbability = this.countPerCycle / this.countCeil;
    this.destination = destination;

    // calculate orthogonal vectors.

    // 1. choose vector most different from path vector to get accurate cross product
    Vector leastSimilarRandomVector;
    double randomVector1Dot = unitPath.dot(RANDOM_VECTOR_1);
    double randomVector2Dot = unitPath.dot(RANDOM_VECTOR_2);
    if (randomVector1Dot < randomVector2Dot) {
      leastSimilarRandomVector = RANDOM_VECTOR_1;
    } else {
      leastSimilarRandomVector = RANDOM_VECTOR_2;
    }
    this.orthogonalUnit1 = unitPath.cross(leastSimilarRandomVector).unit();
    this.orthogonalUnit2 = unitPath.cross(this.orthogonalUnit1).unit();
  }

  /**
   * Update the step with the entity's current location
   *
   * @return the length of the step traversed
   */
  double update() {
    if (done) {
      return 0;
    }
    double startingCompletedLength = completedLength;
    Optional<Vector> maybeLoc = Journey.get().proxy().platform().entityVector(entityUuid);
    if (maybeLoc.isEmpty()) {
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
    double offPathVec;
    double offRadius;  // offset away from center of cross-section
    double offAngle;
    double offVec1;
    double offVec2;
    final double deltaX = unitPath.x() * PARTICLE_UNIT_DISTANCE;
    final double deltaY = unitPath.y() * PARTICLE_UNIT_DISTANCE;
    final double deltaZ = unitPath.z() * PARTICLE_UNIT_DISTANCE;

    // TODO to handle the new particle parameters, we should change the proxy interface to just spawn at
    //  a single location. Otherwise, we leave the randomization to the implementation of the proxy, but we should
    //  be doing the randomization here, and with vector math so we do the width appropriately :)
    //  So basically, do vector math here and pass in a single pre-calculated location for every tick
    // Stop if we reach the end OR the total distance we are displaying is longer than the supposed cached length size (so we're not showing unnecessary particles too far ahead)
    while (distance < totalLength && (distance - startDistance) < TrailNavigator.CACHED_JOURNEY_STEPS_LENGTH) {

      // for the number of times dictated by the input "density", spawn a particle at a random location,
      // spread out as far as the width dictates but only forward as far as the PARTICLE_UNIT_DISTANCE
      for (double i = 0; i < countCeil; i += 1.0) {
        if (random.nextDouble() > this.particleProbability) {
          // ignore this one
          continue;
        }
        offPathVec = random.nextDouble() * PARTICLE_UNIT_DISTANCE;
        offRadius = crossSectionRadius * random.nextDouble();
        offAngle = random.nextDouble() * PI_TIMES_2;
        offVec1 = Math.sin(offAngle) * offRadius;
        offVec2 = Math.cos(offAngle) * offRadius;

        Journey.get().proxy().platform().spawnModeParticle(entityUuid,
            particleType,
            domain,
            curX + (unitPath.x() * offPathVec) + (orthogonalUnit1.x() * offVec1) + (orthogonalUnit2.x() * offVec2),
            curY + (unitPath.y() * offPathVec) + (orthogonalUnit1.y() * offVec1) + (orthogonalUnit2.y() * offVec2),
            curZ + (unitPath.z() * offPathVec) + (orthogonalUnit1.z() * offVec1) + (orthogonalUnit2.z() * offVec2));
      }
      curX += deltaX;
      curY += deltaY;
      curZ += deltaZ;
      distance += PARTICLE_UNIT_DISTANCE;
    }
  }

  public int domain() {
    return domain;
  }

  public Cell destination() {
    return destination;
  }
}
