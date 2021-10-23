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
 */

package edu.whimc.indicator.spigot.journey;

import edu.whimc.indicator.spigot.IndicatorSpigot;
import edu.whimc.indicator.common.journey.Journey;
import edu.whimc.indicator.common.navigation.Itinerary;
import edu.whimc.indicator.common.navigation.Leap;
import edu.whimc.indicator.common.navigation.ModeType;
import edu.whimc.indicator.common.navigation.Path;
import edu.whimc.indicator.common.navigation.Step;
import edu.whimc.indicator.common.search.SearchSession;
import edu.whimc.indicator.common.tools.AlternatingList;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import edu.whimc.indicator.spigot.util.Format;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerJourney implements Journey<LocationCell, World> {

  private static final int ILLUMINATED_COUNT = 64;
  private static final int TICKS_PER_PARTICLE = 3;
  private static final double CHANCE_OF_PARTICLE = 0.4;
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
  private final AlternatingList.Traversal<Leap<LocationCell, World>,
      Path<LocationCell, World>,
      Path<LocationCell, World>> traversal;
  private final SearchSession<LocationCell, World> session;
  private int stepIndex = 0;
  private boolean completed = false;
  private Runnable stopIllumination;
  /**
   * Itinerary that is queued as a better one, in case the player wants to use it.
   */
  private Itinerary<LocationCell, World> prospectiveItinerary;

  public PlayerJourney(@NotNull final UUID playerUuid,
                       @NotNull SearchSession<LocationCell, World> session,
                       @NotNull final Itinerary<LocationCell, World> itinerary) {
    this.playerUuid = playerUuid;
    this.session = session;
    this.itinerary = itinerary;
    this.traversal = itinerary.getStages().traverse();
    startPath(traversal.next()); // start first trail (move beyond the first "leap")
  }

  private LocationCell destination() {
    return itinerary.getSteps().get(itinerary.getSteps().size() - 1).getLocatable();
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
          player.spigot().sendMessage(Format.success("You've reached your destination"));
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
        removing = traversal.get().getSteps().get(stepIndex).getLocatable();
        near.remove(removing);
        stepIndex++;
      } while (!locatable.equals(removing));
      for (int i = originalStepIndex + PROXIMAL_BLOCK_CACHE_SIZE;
           i < Math.min(stepIndex + PROXIMAL_BLOCK_CACHE_SIZE, traversal.get().getSteps().size());
           i++) {
        near.add(traversal.get().getSteps().get(i).getLocatable());
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

  private void startPath(Path<LocationCell, World> path) {
    stepIndex = 0;
    near.clear();
    for (int i = 0; i < Math.min(PROXIMAL_BLOCK_CACHE_SIZE, path.getSteps().size()); i++) {
      near.add(traversal.get().getSteps().get(i).getLocatable());
    }
  }

  public void illuminateTrail() {
    // Set up illumination scheduled task for showing the paths
    Random rand = new Random();
    int illuminationTaskId = Bukkit.getScheduler().runTaskTimer(IndicatorSpigot.getInstance(), () -> {
      PlayerJourney journey = IndicatorSpigot.getInstance()
          .getSearchManager()
          .getJourney(playerUuid);
      if (journey == null) {
        return;
      }

      List<Step<LocationCell, World>> steps = journey.next(ILLUMINATED_COUNT);  // Show 16 steps ahead
      Step<LocationCell, World> step;
      for (int i = 0; i < steps.size() - 1; i++) {
        if (rand.nextDouble() < CHANCE_OF_PARTICLE) {  // make shimmering effect
          Particle particle;
          ModeType modeType = steps.get(i + 1).getModeType();
          step = steps.get(i);
          if (modeType.equals(ModeType.WALK)) {
            particle = Particle.FLAME;
          } else if (modeType.equals(ModeType.JUMP)) {
            particle = Particle.HEART;
          } else {
            particle = Particle.CLOUD;
          }

          spawnParticle(step.getLocatable(), particle, rand.nextFloat(), 0.4f, rand.nextFloat());

          // Check if we need to "hint" where the trail is because the water obscures the particle
          if (step.getLocatable().getBlock().isLiquid()
              && !step.getLocatable().getBlockAtOffset(0, 1, 0).isLiquid()) {
            spawnParticle(step.getLocatable(), particle, rand.nextFloat(), 1.4f, rand.nextFloat());
          }
        }
      }
    }, 0, TICKS_PER_PARTICLE).getTaskId();

    this.stopIllumination = () -> Bukkit.getScheduler().cancelTask(illuminationTaskId);

  }

  private void spawnParticle(LocationCell cell, Particle particle, float xOffset, float yOffset, float zOffset) {
    cell.getDomain().spawnParticle(particle,
        cell.getX() + xOffset,
        cell.getY() + yOffset,
        cell.getZ() + zOffset,
        1,
        0, 0, 0,
        0);
  }

  public SearchSession<LocationCell, World> getSession() {
    return session;
  }
  public void setProspectiveItinerary(Itinerary<LocationCell, World> prospectiveItinerary) {
    this.prospectiveItinerary = prospectiveItinerary;
  }

  public Itinerary<LocationCell, World> getProspectiveItinerary() {
    return prospectiveItinerary;
  }

}