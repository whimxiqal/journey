package edu.whimc.indicator.spigot.journey;

import edu.whimc.indicator.api.journey.Journey;
import edu.whimc.indicator.api.path.Link;
import edu.whimc.indicator.api.path.Path;
import edu.whimc.indicator.api.path.Step;
import edu.whimc.indicator.api.path.Trail;
import edu.whimc.indicator.spigot.path.LocationCell;
import org.bukkit.World;

import java.util.*;

public class PlayerJourney implements Journey<LocationCell, World> {

  private static final int NEAR_SIZE = 20;

  private final UUID playerUuid;
  private final List<Trail<LocationCell, World>> trails;
  private final List<Link<LocationCell, World>> links;
  private final Set<LocationCell> near = new HashSet<>();
  private int trailIndex = 0;
  private int stepIndex = 0;

  public PlayerJourney(final UUID playerUuid, final Path<LocationCell, World> path) {
    this.playerUuid = playerUuid;
    this.trails = path.getTrailsCopy();
    this.links = path.getDomainLinksCopy();
    if (trails.size() == 0) {
      throw new IllegalStateException("The journey may not have 0 trails");
    }
    startTrail(); // start first trail
  }

  @Override
  public void visit(LocationCell locatable) {
    if (trails.get(trailIndex).getDestination().equals(locatable)) {
      trailIndex++;
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
  public Collection<Step<LocationCell, World>> next(int count) {
    return null;
  }

  @Override
  public LocationCell closest(LocationCell other) {
    return null;
  }


  private void startTrail() {
    near.clear();
    for (int i = 0; i < Math.min(NEAR_SIZE, trails.get(trailIndex).getSteps().size()); i++) {
      near.add(trails.get(trailIndex).getSteps().get(i).getLocatable());
    }
  }

}
