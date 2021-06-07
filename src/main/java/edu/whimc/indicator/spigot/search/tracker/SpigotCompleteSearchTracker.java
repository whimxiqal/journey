package edu.whimc.indicator.spigot.search.tracker;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.path.*;
import edu.whimc.indicator.common.search.tracker.SearchTracker;
import edu.whimc.indicator.spigot.journey.PlayerJourney;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.util.Format;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class SpigotCompleteSearchTracker implements SearchTracker<LocationCell, World> {

  private static final int ILLUMINATED_COUNT = 16;
  private static final int TICKS_PER_PARTICLE = 3;
  private static final double CHANCE_OF_PARTICLE = 0.4;

  private final UUID playerUuid;
  private final LinkedList<SearchTracker<LocationCell, World>> trackers;
  boolean foundPath = false;
  int successNotificationTaskId = 0;
  boolean sentSuccessNotification = false;

  private SpigotCompleteSearchTracker(UUID playerUuid, LinkedList<SearchTracker<LocationCell, World>> list) {
    this.playerUuid = playerUuid;
    this.trackers = list;
  }

  public static SpigotCompleteSearchTracker.Builder builder(Player player) {
    return new Builder(player);
  }

  @Override
  public void acceptResult(LocationCell cell, Result result, ModeType modeType) {
    trackers.forEach(tracker -> tracker.acceptResult(cell, result, modeType));
  }

  @Override
  public void foundNewOptimalPath(Path<LocationCell, World> path, Completion<LocationCell, World> completion) {
    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null) {
      return;
    }

    if (sentSuccessNotification) {
      // TODO do something different for subsequent found paths
      player.spigot().sendMessage(Format.info("A faster path to your destination was found..."));
      player.spigot().sendMessage(Format.info("You may use this feature in later versions."));
      return;
    }

    if (foundPath) {
      Bukkit.getScheduler().cancelTask(successNotificationTaskId);
    }
    foundPath = true;

    // Set up illumination scheduled task for showing the trails
    Random rand = new Random();
    int illuminationTaskId = Bukkit.getScheduler().runTaskTimer(Indicator.getInstance(), () -> {
      Optional<PlayerJourney> journeyOptional = Indicator.getInstance()
          .getJourneyManager()
          .getPlayerJourney(playerUuid);
      if (!journeyOptional.isPresent()) return;
      PlayerJourney journey = journeyOptional.get();

      List<Step<LocationCell, World>> steps = journey.next(ILLUMINATED_COUNT);  // Show 16 steps ahead
      for (int i = 0; i < steps.size() - 1; i++) {
        if (rand.nextDouble() < CHANCE_OF_PARTICLE) {  // make shimmering effect
          Particle particle;
          ModeType modeType = steps.get(i + 1).getModeType();
          if (modeType.equals(ModeTypes.WALK)) {
            particle = Particle.FLAME;
          } else if (modeType.equals(ModeTypes.JUMP)) {
            particle = Particle.HEART;
          } else {
            particle = Particle.CLOUD;
          }
          steps.get(i).getLocatable().getDomain().spawnParticle(particle,
              steps.get(i).getLocatable().getX() + rand.nextDouble(),
              steps.get(i).getLocatable().getY() + 0.4f,
              steps.get(i).getLocatable().getZ() + rand.nextDouble(),
              1,
              0, 0, 0,
              0);
        }
      }
    }, 0, TICKS_PER_PARTICLE).getTaskId();

    // Create a journey that is completed when the player reaches within 3 blocks of the endpoint
    PlayerJourney journey = new PlayerJourney(playerUuid, path, completion);
    journey.setIlluminationTaskId(illuminationTaskId);

    // Save the journey and stop the illumination from the other one
    Indicator.getInstance().getJourneyManager().putPlayerJourney(playerUuid, journey);

    // Set up a success notification that will be cancelled if a better one is found in some amount of time
    successNotificationTaskId = Bukkit.getScheduler()
        .runTaskLater(Indicator.getInstance(),
            () -> {
              player.spigot().sendMessage(Format.success("Showing a path to your destination"));
              sentSuccessNotification = true;
            },
            20 /* ticks per second */ / 2)
        .getTaskId();
    trackers.forEach(tracker -> tracker.foundNewOptimalPath(path, completion));
  }

  @Override
  public void startTrailSearch(LocationCell origin, LocationCell destination) {
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.PREFIX + Format.WARN + "Began" + Format.DEBUG + " a trail search: ");
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(
        origin.toString()
            + " -> "));
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(destination.toString()));
    trackers.forEach(tracker -> tracker.startTrailSearch(origin, destination));
  }

  @Override
  public void trailSearchVisitation(Step<LocationCell, World> step) {
    // Nothing?
    trackers.forEach(tracker -> tracker.trailSearchVisitation(step));
  }

  @Override
  public void trailSearchStep(Step<LocationCell, World> step) {
    // Nothing?
    trackers.forEach(tracker -> tracker.trailSearchStep(step));
  }

  @Override
  public void finishTrailSearch(LocationCell origin, LocationCell destination, double distance) {
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.PREFIX + Format.SUCCESS + "Finished" + Format.DEBUG + " a trail search: ");
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(
        origin.toString()
            + " -> "));
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(destination.toString()));
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug("Length: " + (distance > 1000000 ? "Inf" : Math.round(distance))));
    trackers.forEach(tracker -> tracker.finishTrailSearch(origin, destination, distance));

  }

  @Override
  public void memoryCapacityReached(LocationCell origin, LocationCell destination) {
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug("Ran out of allocated memory for a local trail search: "));
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(
        origin.toString()
            + " -> "));
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(destination.toString()));
    trackers.forEach(tracker -> tracker.memoryCapacityReached(origin, destination));

  }

  public static class Builder {

    private final LinkedList<SearchTracker<LocationCell, World>> trackers = new LinkedList<>();
    private final UUID playerUuid;

    private Builder(Player player) {
      this.playerUuid = player.getUniqueId();
    }

    public SpigotCompleteSearchTracker build() {
      return new SpigotCompleteSearchTracker(this.playerUuid, trackers);
    }

    public Builder animate(int delayMillis) {
      trackers.add(new SpigotSearchAnimator(this.playerUuid, delayMillis));
      return this;
    }

    public Builder trackData() {
      trackers.add(new SpigotSearchDataTracker());
      return this;
    }

  }
}
