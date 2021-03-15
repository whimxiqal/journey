package edu.whimc.indicator.spigot.journey;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.api.journey.Journey;
import edu.whimc.indicator.api.path.*;
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
      Indicator.getInstance().getLogger().info("Completed journey");
      if (Indicator.getInstance().getDebugManager().isDebugging(playerUuid)) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null) {
          player.sendMessage(Format.debug("Reached destination: " + trails.get(trails.size() - 1).getDestination()));
        }
      }
      completed = true;
      return;
    }
    if (trailIndex < trails.size() - 1) {
      // Our trail destination is a link
      if (links.get(trailIndex).getCompletion().test(locatable)) {
        // We reached the link
        if (Indicator.getInstance().getDebugManager().isDebugging(playerUuid)) {
          Player player = Bukkit.getPlayer(playerUuid);
          if (player != null) {
            player.sendMessage(Format.debug("Reached destination: " + links.get(trailIndex)));
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
  public LocationCell closest(LocationCell other) {
    // TODO implement
    return null;
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
