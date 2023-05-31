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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.message.Formatter;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.navigation.Path;
import net.whimxiqal.journey.navigation.Step;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.tools.AlternatingList;
import org.jetbrains.annotations.NotNull;

public class PlayerJourneySession implements JourneySession {

  public static final double CACHED_JOURNEY_STEPS_LENGTH = 128;  // length of all journey steps to cache for showing their particles
  public static final int TICKS_PER_PARTICLE_CYCLE = 3;
  public static final double PARTICLE_UNIT_DISTANCE = 0.5;  // number of blocks between which particles will be shown
  public static final int PARTICLE_CYCLE_COUNT = 1;
  public static final float PARTICLE_SPAWN_DENSITY = 0.6f;

  private final UUID playerUuid;
  private final LinkedList<JourneyStep> journeySteps = new LinkedList<>();
  private final SearchSession session;
  private final Itinerary itinerary;
  private int lastAddedJourneyStepIndex = 0;
  private double journeyStepsLength = 0;
  private State state = State.STOPPED_INCOMPLETE;
  private UUID illuminationTaskId;
  private AlternatingList.Traversal<Path, Path, Path> traversal;

  /**
   * General constructor.
   *
   * @param playerUuid the identifier for the player
   * @param search     the player search
   * @param itinerary  the itinerary that determines the path
   */
  public PlayerJourneySession(@NotNull final UUID playerUuid,
                              @NotNull SearchSession search,
                              @NotNull final Itinerary itinerary) {
    this.session = search;
    this.itinerary = itinerary;
    this.traversal = itinerary.getStages().traverse();
    this.playerUuid = playerUuid;
  }

  /**
   * Get the player search session used to calculate this journey.
   *
   * @return the session
   */
  public SearchSession getSession() {
    return session;
  }

  /**
   * Get the itinerary that originally determined the directions
   * and acts as a roadmap for this journey.
   *
   * @return the itinerary
   */
  public Itinerary getItinerary() {
    return this.itinerary;
  }

  /**
   * Get the destination of the current path being traversed.
   *
   * @return the destination location
   */
  public final Cell currentPathDestination() {
    return traversal.get().getDestination();
  }

  protected final AlternatingList.Traversal<Path,
      Path,
      Path> traversal() {
    return traversal;
  }

  protected final void resetTraversal() {
    this.traversal = getItinerary().getStages().traverse();
  }

  @Override
  public void visit(Cell locatable) {
    if (state != State.RUNNING) {
      return;
    }
    while (traversal().get().completedWith(locatable) || traversal().get().getCost() == 0) {
      // We have reached our destination for the given path
      if (traversal().hasNext()) {
        // There is another path after this one, move on to the next one
        traversal().next();
        traversal().get().runPrompt();
        startPath();
      } else {
        state = State.STOPPED_COMPLETE;

        // There is no other path after this one, we are done
        Journey.get().proxy().audienceProvider().player(playerUuid).sendMessage(Formatter.success("You've arrived!"));

        // Play a fun chord
        Journey.get().proxy().platform().playSuccess(playerUuid);

        stop();
        return;
      }
    }

    // see if we are done with the current step(s)
    boolean madeProgress = false;
    while (!journeySteps.isEmpty()) {
      JourneyStep next = journeySteps.peek();
      double progress = next.update();
      if (progress > 0) {
        madeProgress = true;
      }
      journeyStepsLength -= progress;
      Journey.get().statsManager().addBlocksTravelled(progress);
      if (!next.done()) {
        break;
      }
      journeySteps.pop();
    }

    // we haven't made any progress this step, so let's try the more calculation-intensive
    // block-by-block search to see if the player has walked into a future location
    if (!madeProgress) {
      int i = 0;
      double closestDistanceSquared = locatable.distanceToSquared(traversal.get().getDestination());
      boolean foundLocation = false;
      for (JourneyStep step : journeySteps) {
        if (locatable.domain() == step.domain()) {
          closestDistanceSquared = Math.min(closestDistanceSquared, locatable.distanceToSquared(step.destination()));
        }
        if (locatable.equals(step.destination())) {
          foundLocation = true;
          break;
        }
        ++i;
      }
      if (foundLocation) {
        // go back and remove every one up to index i
        for (int j = 0; j <= i; j++) {
          journeySteps.pop();
        }
        // recalculate journeyStepsLength
        journeyStepsLength = journeySteps.stream().mapToDouble(JourneyStep::length).sum();
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
  public void stop() {
    if (illuminationTaskId != null) {
      Journey.get().proxy().schedulingManager().cancelTask(illuminationTaskId);
    }
    if (state == State.RUNNING) {
      state = State.STOPPED_INCOMPLETE;
    }
  }

  @Override
  public void run() {
    stop();
    resetTraversal();
    traversal().next();
    illuminateTrail();
    startPath();
    Journey.get().proxy().platform().entityCellLocation(playerUuid).ifPresent(this::visit);
  }

  private void startPath() {
    state = State.RUNNING;
    lastAddedJourneyStepIndex = 0;
    journeySteps.clear();
  }

  private void illuminateTrail() {
    // Set up illumination scheduled task for showing the paths
    illuminationTaskId = Journey.get().proxy().schedulingManager().scheduleRepeat(() -> {
      // Illuminate destination of path
      Cell pathDestination = currentPathDestination();
      Journey.get().proxy().platform().spawnDestinationParticle(
          playerUuid,
          pathDestination.domain(),
          pathDestination.blockX() + 0.5,
          pathDestination.blockY() + 0.4f,
          pathDestination.blockZ() + 0.5,
          PARTICLE_CYCLE_COUNT * 2,
          PARTICLE_SPAWN_DENSITY, PARTICLE_SPAWN_DENSITY, PARTICLE_SPAWN_DENSITY);

      // Illuminate the rest of the path
      for (JourneyStep jStep : journeySteps) {
        jStep.illuminate();
      }
    }, false, TICKS_PER_PARTICLE_CYCLE);

  }

  public boolean running() {
    return state == State.RUNNING;
  }

  private enum State {
    STOPPED_INCOMPLETE,  // hasn't run yet at all
    RUNNING, // is currently running
    STOPPED_COMPLETE, // has finished running successfully
  }

}
