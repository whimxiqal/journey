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

package net.whimxiqal.journey.common.navigation.journey;

import java.util.ArrayList;
import java.util.Queue;
import net.whimxiqal.journey.common.Journey;
import net.whimxiqal.journey.common.message.Formatter;
import net.whimxiqal.journey.common.navigation.AbstractJourneySession;
import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.journey.common.navigation.Itinerary;
import net.whimxiqal.journey.common.navigation.Step;
import net.whimxiqal.journey.common.search.SearchSession;
import java.util.LinkedList;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

public class PlayerJourneySession extends AbstractJourneySession {

  public static final double CACHED_JOURNEY_STEPS_LENGTH = 128;  // length of all journey steps to cache for showing their particles
  public static final int TICKS_PER_PARTICLE_CYCLE = 2;
  public static final double PARTICLE_UNIT_DISTANCE = 0.5;  // number of blocks between which particles will be shown
  public static final int PARTICLE_CYCLE_COUNT = 1;
  public static final float PARTICLE_SPAWN_DENSITY = 0.6f;

  private final UUID playerUuid;
  private final Queue<JourneyStep> journeySteps = new LinkedList<>();
  private int lastAddedJourneyStepIndex = 0;
  private double journeyStepsLength = 0;
  private State state = State.STOPPED_INCOMPLETE;
  private UUID illuminationTaskId;

  private enum State {
    STOPPED_INCOMPLETE,
    RUNNING,
    STOPPED_COMPLETE
  }

  /**
   * General constructor.
   *
   * @param playerUuid the identifier for the player
   * @param search    the player search
   * @param itinerary  the itinerary that determines the path
   */
  public PlayerJourneySession(@NotNull final UUID playerUuid,
                              @NotNull SearchSession search,
                              @NotNull final Itinerary itinerary) {
    super(search, itinerary);
    this.playerUuid = playerUuid;
  }

  @Override
  public void visit(Cell locatable) {
    if (state != State.RUNNING) {
      return;
    }
    if (traversal().get().completeWith(locatable)) {
      // We have reached our destination for the given path
      if (traversal().hasNext()) {
        // There is another path after this one, move on to the next one
        traversal().next();
        startPath();
      } else {
        // There is no other path after this one, we are done
        Audience audience = Journey.get().proxy().audienceProvider().player(playerUuid);
        audience.sendMessage(Formatter.success("You've arrived!"));

        // Play a fun chord
        Journey.get().proxy().platform().playSuccess(playerUuid);

        state = State.STOPPED_COMPLETE;
        stopAnimating();
        return;
      }
    }
    // see if we are done with the current step(s)
    while (!journeySteps.isEmpty()) {
      JourneyStep next = journeySteps.peek();
      journeyStepsLength -= next.update();
      if (next.done()) {
        journeySteps.remove();
      } else {
        break;
      }
    }

    // add next steps from the traversal
    ArrayList<Step> steps = traversal().get().getSteps();
    while (journeyStepsLength < CACHED_JOURNEY_STEPS_LENGTH && lastAddedJourneyStepIndex + 1 < traversal().get().getSteps().size()) {
      Cell origin = steps.get(lastAddedJourneyStepIndex).location();
      Step nextStep = steps.get(lastAddedJourneyStepIndex + 1);
      JourneyStep jStep = new JourneyStep(playerUuid, origin, nextStep.location(), nextStep.modeType());
      journeySteps.add(jStep);
      journeyStepsLength += jStep.length();
      lastAddedJourneyStepIndex++;
    }
  }

  @Override
  public void stopAnimating() {
    Journey.get().proxy().schedulingManager().cancelTask(illuminationTaskId);
  }

  @Override
  public void run() {
    stopAnimating();
    resetTraversal();
    traversal().next();
    startPath();
    illuminateTrail();
  }

  private void startPath() {
    state = State.RUNNING;
    lastAddedJourneyStepIndex = 0;
    journeySteps.clear();
    Journey.get().proxy().platform().entityCellLocation(playerUuid).ifPresent(this::visit);
  }

  private void illuminateTrail() {
    // Set up illumination scheduled task for showing the paths
    illuminationTaskId = Journey.get().proxy().schedulingManager().scheduleRepeat(() -> {
      // Illuminate destination of path
      Cell pathDestination = currentPathDestination();
      Journey.get().proxy().platform().spawnDestinationParticle(
          playerUuid,
          pathDestination.domainId(),
          pathDestination.getX() + 0.5,
          pathDestination.getY() + 0.4f,
          pathDestination.getZ() + 0.5,
          PARTICLE_CYCLE_COUNT * 2,
          PARTICLE_SPAWN_DENSITY, PARTICLE_SPAWN_DENSITY, PARTICLE_SPAWN_DENSITY);

      // Illuminate the rest of the path
      for (JourneyStep jStep : journeySteps) {
        jStep.illuminate();
      }
    }, false, TICKS_PER_PARTICLE_CYCLE);

  }

  /**
   * Determine whether a player's journey has been completed by the player.
   *
   * @return true if complete
   */
  public boolean isCompleted() {
    return state == State.STOPPED_COMPLETE;
  }

}
