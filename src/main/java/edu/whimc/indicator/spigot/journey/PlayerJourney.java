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

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.journey.Journey;
import edu.whimc.indicator.common.path.*;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.util.Format;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerJourney implements Journey<LocationCell, World> {

  private static final int ILLUMINATED_COUNT = 32;
  private static final int TICKS_PER_PARTICLE = 3;
  private static final double CHANCE_OF_PARTICLE = 0.4;
  private static final int PROXIMAL_BLOCK_CACHE_SIZE = 128;

  @Getter @Setter
  private Path<LocationCell, World> prospectivePath;

  private final UUID playerUuid;
  private final List<Trail<LocationCell, World>> trails;
  private final List<Link<LocationCell, World>> links;
  private final LocationCell destination;
  private final Completion<LocationCell, World> completion;
  private final Set<LocationCell> near = new HashSet<>();
  private int trailIndex = 0;
  private int stepIndex = 0;
  private boolean completed = false;
  private Runnable stopIllumination;

  public PlayerJourney(@NotNull final UUID playerUuid,
                       @NotNull final Path<LocationCell, World> path) {
    this.playerUuid = playerUuid;
    this.trails = path.getTrailsCopy();
    this.links = path.getDomainLinksCopy();
    this.destination = trails.get(trails.size() - 1).getDestination();
    this.completion = (cell) -> cell.distanceToSquared(destination) < 9;
    if (trails.size() == 0) {
      throw new IllegalStateException("The journey may not have 0 trails");
    }
    startTrail(); // start first trail
  }

  @Override
  public void visit(LocationCell locatable) {
    if (completed) {
      return;  // We're already done, we don't care about visitation
    }
    if (trails.get(trailIndex).getDestination() == null) {
      throw new IllegalStateException("Could not get the destination of trail");
    }
    // Check if we finished
    if (completion.test(locatable)) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null) {
          player.spigot().sendMessage(Format.success("You've reached your destination"));
        }
      completed = true;
      stop();
      return;
    }
    if (trailIndex < trails.size() - 1) {
      // Our trail destination is a link
      if (links.get(trailIndex).getCompletion().test(locatable)) {
        // We reached the link
        if (Indicator.getInstance().getDebugManager().isDebugging(playerUuid)) {
          Player player = Bukkit.getPlayer(playerUuid);
          if (player != null) {
            player.spigot().sendMessage(Format.debug("Reached destination: " + links.get(trailIndex)));
          }
        }
        trailIndex++;
        startTrail();
      }
    }
    if (near.contains(locatable)) {
      int originalStepIndex = stepIndex;
      LocationCell removing;
      do {
        removing = trails.get(trailIndex).getSteps().get(stepIndex).getLocatable();
        near.remove(removing);
        stepIndex++;
      } while (!locatable.equals(removing));
      for (int i = originalStepIndex + PROXIMAL_BLOCK_CACHE_SIZE;
           i < Math.min(stepIndex + PROXIMAL_BLOCK_CACHE_SIZE, trails.get(trailIndex).getSteps().size());
           i++) {
        near.add(trails.get(trailIndex).getSteps().get(i).getLocatable());
      }
    }
  }

  @Override
  public List<Step<LocationCell, World>> next(int count) {
    if (completed) {
      return new LinkedList<>();  // Nothing left
    }
    if (count > PROXIMAL_BLOCK_CACHE_SIZE) {
      throw new IllegalArgumentException("The count may not be larger than " + PROXIMAL_BLOCK_CACHE_SIZE);
    }
    List<Step<LocationCell, World>> next = new LinkedList<>();
    for (int i = stepIndex; i < Math.min(stepIndex + count, trails.get(trailIndex).getSteps().size()); i++) {
      next.add(trails.get(trailIndex).getSteps().get(i));
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

  private void startTrail() {
    stepIndex = 0;
    near.clear();
    for (int i = 0; i < Math.min(PROXIMAL_BLOCK_CACHE_SIZE, trails.get(trailIndex).getSteps().size()); i++) {
      near.add(trails.get(trailIndex).getSteps().get(i).getLocatable());
    }
  }

  public void illuminateTrail() {

    // Set up illumination scheduled task for showing the trails
    Random rand = new Random();
    int illuminationTaskId = Bukkit.getScheduler().runTaskTimer(Indicator.getInstance(), () -> {
      PlayerJourney journey = Indicator.getInstance()
          .getSearchManager()
          .getPlayerJourney(playerUuid);
      if (journey == null) return;

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

}
