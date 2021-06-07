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
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerJourney implements Journey<LocationCell, World> {

  private static final int NEAR_SIZE = 100;

  private final UUID playerUuid;
  private final List<Trail<LocationCell, World>> trails;
  private final List<Link<LocationCell, World>> links;
  private final Set<LocationCell> near = new HashSet<>();
  private final Completion<LocationCell, World> completion;
  private int trailIndex = 0;
  private int stepIndex = 0;
  private boolean completed = false;

  @Setter @Getter
  private int illuminationTaskId;

  public PlayerJourney(final UUID playerUuid,
                       final Path<LocationCell, World> path,
                       final Completion<LocationCell, World> completion) {
    this.playerUuid = playerUuid;
    this.trails = path.getTrailsCopy();
    this.links = path.getDomainLinksCopy();
    this.completion = completion;
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
      if (Indicator.getInstance().getDebugManager().isDebugging(playerUuid)) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null) {
          player.spigot().sendMessage(Format.debug("Reached destination: " + trails.get(trails.size() - 1).getDestination()));
        }
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
      for (int i = originalStepIndex + NEAR_SIZE;
           i < Math.min(stepIndex + NEAR_SIZE, trails.get(trailIndex).getSteps().size());
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
    if (count > NEAR_SIZE) {
      throw new IllegalArgumentException("The count may not be larger than " + NEAR_SIZE);
    }
    List<Step<LocationCell, World>> next = new LinkedList<>();
    for (int i = stepIndex; i < Math.min(stepIndex + count, trails.get(trailIndex).getSteps().size()); i++) {
      next.add(trails.get(trailIndex).getSteps().get(i));
    }
    return next;
  }

  @Override
  public void stop() {
    Bukkit.getScheduler().cancelTask(this.getIlluminationTaskId());
  }

  @Override
  public boolean isCompleted() {
    return completed;
  }

  private void startTrail() {
    stepIndex = 0;
    near.clear();
    for (int i = 0; i < Math.min(NEAR_SIZE, trails.get(trailIndex).getSteps().size()); i++) {
      near.add(trails.get(trailIndex).getSteps().get(i).getLocatable());
    }
  }

}
