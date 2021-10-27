/*
 * Copyright 2021 Pieter Svenson
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
 *
 */

package edu.whimc.journey.spigot.navigation;

import edu.whimc.journey.common.navigation.Itinerary;
import edu.whimc.journey.common.navigation.Journey;
import edu.whimc.journey.common.navigation.ModeType;
import edu.whimc.journey.common.navigation.Path;
import edu.whimc.journey.common.navigation.Port;
import edu.whimc.journey.common.navigation.Step;
import edu.whimc.journey.common.tools.AlternatingList;
import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.music.Song;
import edu.whimc.journey.spigot.search.PlayerSearchSession;
import edu.whimc.journey.spigot.util.Format;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A journey given to a {@link Player}.
 */
public class PlayerJourney implements Journey<LocationCell, World> {

  private static final int ILLUMINATED_COUNT = 64;

  private static final int PARTICLE_CYCLE_COUNT = 4;
  private static final int TICKS_PER_PARTICLE_CYCLE = 2;

  private static final float PARTICLE_SPAWN_DENSITY = 0.6f;
  private static final int PROXIMAL_BLOCK_CACHE_SIZE = 128;
  private final UUID playerUuid;
  /**
   * The set of all locations that are near the player.
   * This set will contain all locations ahead of the player
   * and once the player reaches one of the locations in the set,
   * all locations prior will be removed and the next bunch will be
   * added ahead so the trail continues forging ahead.
   */
  private final Set<LocationCell> near = new HashSet<>();
  private final Itinerary<LocationCell, World> itinerary;
  private final PlayerSearchSession session;
  private AlternatingList.Traversal<Port<LocationCell, World>,
      Path<LocationCell, World>,
      Path<LocationCell, World>> traversal;
  private int stepIndex = 0;
  private boolean completed = false;
  private Runnable stopIllumination;
  /**
   * Itinerary that is queued as a better one, in case the player wants to use it.
   */
  private Itinerary<LocationCell, World> prospectiveItinerary;

  /**
   * General constructor.
   *
   * @param playerUuid the identifier for the player
   * @param session    the search session
   * @param itinerary  the itinerary that determines the path
   */
  public PlayerJourney(@NotNull final UUID playerUuid,
                       @NotNull PlayerSearchSession session,
                       @NotNull final Itinerary<LocationCell, World> itinerary) {
    this.playerUuid = playerUuid;
    this.session = session;
    this.itinerary = itinerary;
    this.traversal = itinerary.getStages().traverse();
    startPath(traversal.next()); // start first trail (move beyond the first "leap")
  }

  @Override
  public void visit(LocationCell locatable) {
    if (completed) {
      return;  // We're already done, we don't care about visitation
    }
    if (traversal.get().completeWith(locatable)) {
      // We have reached our destination for the given path
      if (traversal.hasNext()) {
        // There is another path after this one, move on to the next one
        startPath(traversal.next());
      } else {
        // There is no other path after this one, we are done
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null) {
          player.spigot().sendMessage(Format.success("You've arrived!"));

          // Play a fun chord
          Song.SUCCESS_CHORD.play(player);
        }
        completed = true;
        stop();
        return;
      }
    }
    if (near.contains(locatable)) {
      int originalStepIndex = stepIndex;
      LocationCell removing;
      do {
        removing = traversal.get().getSteps().get(stepIndex).location();
        near.remove(removing);
        stepIndex++;
      } while (!locatable.equals(removing));
      for (int i = originalStepIndex + PROXIMAL_BLOCK_CACHE_SIZE;
           i < Math.min(stepIndex + PROXIMAL_BLOCK_CACHE_SIZE, traversal.get().getSteps().size());
           i++) {
        near.add(traversal.get().getSteps().get(i).location());
      }
    }
  }

  @Override
  public List<Step<LocationCell, World>> next(int count) {
    if (count > PROXIMAL_BLOCK_CACHE_SIZE) {
      throw new IllegalArgumentException("The count may not be larger than " + PROXIMAL_BLOCK_CACHE_SIZE);
    }
    if (completed) {
      return new LinkedList<>();  // Nothing left
    }
    List<Step<LocationCell, World>> next = new LinkedList<>();
    for (int i = stepIndex; i < Math.min(stepIndex + count, traversal.get().getSteps().size()); i++) {
      next.add(traversal.get().getSteps().get(i));
    }
    return next;
  }

  @Override
  public void stop() {
    stopIllumination.run();
  }

  @Override
  public boolean isCompleted() {
    return completed;
  }

  @Override
  public LocationCell currentPathDestination() {
    return traversal.get().getDestination();
  }

  @Override
  public void run() {
    stop();
    completed = false;
    traversal = itinerary.getStages().traverse();
    startPath(traversal.next());
    illuminateTrail();
  }

  private void startPath(Path<LocationCell, World> path) {
    stepIndex = 0;
    near.clear();
    for (int i = 0; i < Math.min(PROXIMAL_BLOCK_CACHE_SIZE, path.getSteps().size()); i++) {
      near.add(path.getSteps().get(i).location());
    }
  }

  private void illuminateTrail() {
    // Set up illumination scheduled task for showing the paths
    int illuminationTaskId = Bukkit.getScheduler().runTaskTimer(JourneySpigot.getInstance(), () -> {
      PlayerJourney journey = JourneySpigot.getInstance()
          .getSearchManager()
          .getJourney(playerUuid);
      if (journey == null) {
        return;
      }

      // Illuminate destination of path
      LocationCell pathDestination = journey.currentPathDestination();
      pathDestination.getDomain().spawnParticle(Particle.SPELL_WITCH,
          pathDestination.getX() + 0.5,
          pathDestination.getY() + 0.4f,
          pathDestination.getZ() + 0.5,
          PARTICLE_CYCLE_COUNT * 2,
          PARTICLE_SPAWN_DENSITY, PARTICLE_SPAWN_DENSITY, PARTICLE_SPAWN_DENSITY,
          0);

      // Illuminate the rest of the path
      List<Step<LocationCell, World>> steps = journey.next(ILLUMINATED_COUNT);  // Show 16 steps ahead
      Step<LocationCell, World> step;
      for (int i = 0; i < steps.size() - 1; i++) {
        Particle particle;
        ModeType modeType = steps.get(i + 1).getModeType();
        step = steps.get(i);
        if (modeType == ModeType.FLY) {
          particle = Particle.WAX_OFF;
        } else {
          particle = Particle.GLOW;
        }

        step.location().getDomain().spawnParticle(particle,
            step.location().getX() + 0.5,
            step.location().getY() + 0.4f,
            step.location().getZ() + 0.5,
            PARTICLE_CYCLE_COUNT,
            PARTICLE_SPAWN_DENSITY, PARTICLE_SPAWN_DENSITY, PARTICLE_SPAWN_DENSITY,
            0);

        // Check if we need to "hint" where the trail is because the water obscures the particle
        if (step.location().getBlock().isLiquid()
            && !step.location().getBlockAtOffset(0, 1, 0).isLiquid()) {
          step.location().getDomain().spawnParticle(particle,
              step.location().getX() + 0.5f,
              step.location().getY() + 1.4f,
              step.location().getZ() + 0.5f,
              PARTICLE_CYCLE_COUNT,
              PARTICLE_SPAWN_DENSITY, PARTICLE_SPAWN_DENSITY, PARTICLE_SPAWN_DENSITY,
              0);
        }
      }
    }, 0, TICKS_PER_PARTICLE_CYCLE).getTaskId();

    this.stopIllumination = () -> Bukkit.getScheduler().cancelTask(illuminationTaskId);

  }

  /**
   * Get the player search session used to calculate this journey.
   *
   * @return the session
   */
  public PlayerSearchSession getSession() {
    return session;
  }

  /**
   * Get the prospective itinerary.
   * This (better) itinerary was calculated by the session after this journey was already prepared
   * for the user. The user will have the option of using this itinerary if he/she
   * so chooses and create another journey.
   *
   * @return the itinerary
   */
  public Itinerary<LocationCell, World> getProspectiveItinerary() {
    return prospectiveItinerary;
  }

  /**
   * Get the prospective itinerary.
   * This (better) itinerary was calculated by the session after this journey was already prepared
   * for the user. The user will have the option of using this itinerary if he/she
   * so chooses and create another journey.
   *
   * @param prospectiveItinerary the prospective itinerary
   */
  public void setProspectiveItinerary(Itinerary<LocationCell, World> prospectiveItinerary) {
    this.prospectiveItinerary = prospectiveItinerary;
  }

}
