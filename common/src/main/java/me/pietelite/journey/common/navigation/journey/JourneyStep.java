package me.pietelite.journey.common.navigation.journey;

import java.util.Optional;
import java.util.UUID;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.math.Vector;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.ModeType;
import me.pietelite.journey.common.navigation.Moded;
import org.jetbrains.annotations.NotNull;

import static me.pietelite.journey.common.navigation.journey.PlayerJourneySession.*;

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
