package edu.whimc.indicator.spigot.search.tracker;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.navigation.*;
import edu.whimc.indicator.common.search.Search;
import edu.whimc.indicator.common.search.tracker.BlankSearchTracker;
import edu.whimc.indicator.spigot.journey.PlayerJourney;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import edu.whimc.indicator.spigot.util.Format;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SpigotSearchTracker extends BlankSearchTracker<LocationCell, World> {

  @Nullable
  private final UUID playerUuid;
  /**
   * True if the algorithm has found a path.
   */
  boolean foundPath = false;
  int successNotificationTaskId = 0;
  /**
   * True if the player has been presented with a path.
   */
  boolean presentedPath = false;

  public SpigotSearchTracker() {
    this.playerUuid = null;
  }

  public SpigotSearchTracker(@NotNull UUID playerUuid) {
    this.playerUuid = playerUuid;
  }

  @Override
  public void searchStarted(Search<LocationCell, World> search) {
    if (playerUuid != null) {
      Indicator.getInstance().getSearchManager().putSearch(playerUuid, search);
    }
  }

  @Override
  public void foundNewOptimalPath(Itinerary itinerary) {
    if (playerUuid == null) {
      return;
    }
    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null) {
      return;
    }

    if (presentedPath) {
      Indicator.getInstance().getSearchManager().getPlayerJourney(player.getUniqueId()).setProspectiveItinerary(itinerary);
      player.spigot().sendMessage(Format.info("A faster itinerary to your destination was found from your original location"));
      player.spigot().sendMessage(Format.chain(Format.info("Run "),
          Format.command("/trail accept", "Accept an incoming trail request"),
          Format.textOf(" to accept")));
      return;
    }

    if (foundPath) {
      Bukkit.getScheduler().cancelTask(successNotificationTaskId);
    }
    foundPath = true;

    // Create a journey that is completed when the player reaches within 3 blocks of the endpoint
    PlayerJourney journey = new PlayerJourney(playerUuid, itinerary);
    journey.illuminateTrail();

    // Save the journey
    Indicator.getInstance().getSearchManager().putPlayerJourney(playerUuid, journey);

    // Set up a success notification that will be cancelled if a better one is found in some amount of time
    successNotificationTaskId = Bukkit.getScheduler()
        .runTaskLater(Indicator.getInstance(),
            () -> {
              player.spigot().sendMessage(Format.success("Showing a itinerary to your destination"));
              presentedPath = true;
            },
            20 /* ticks per second */ * 2 /* seconds */)
        .getTaskId();
  }

  @Override
  public void startTrailSearch(LocationCell origin, LocationCell destination) {
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.PREFIX + Format.WARN + "Began" + Format.DEBUG + " a trail search: ");
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(
        origin.toString()
            + " -> "));
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(destination.toString()));
  }

  @Override
  public void completeTrailSearch(LocationCell origin, LocationCell destination, double distance) {
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.PREFIX + Format.SUCCESS + "Finished" + Format.DEBUG + " a trail search: ");
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(
        origin.toString()
            + " -> "));
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(destination.toString()));
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug("Length: " + (distance > 1000000 ? "Inf" : Math.round(distance))));
  }

  @Override
  public void memoryCapacityReached(LocationCell origin, LocationCell destination) {
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug("Ran out of allocated memory for a local trail search: "));
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(
        origin.toString()
            + " -> "));
    Indicator.getInstance().getDebugManager().broadcastDebugMessage(Format.debug(destination.toString()));
  }

  @Override
  public void searchStopped(Search<LocationCell, World> search) {
    // Send failure message if we finished unsuccessfully
    if (playerUuid != null) {
      Player player = Bukkit.getPlayer(playerUuid);
      if (player != null) {
        if (!search.isSuccessful() && !search.isCancelled()) {
          player.spigot().sendMessage(Format.error("A path to your destination could not be found."));
          Indicator.getInstance().getSearchManager().removePlayerJourney(playerUuid);
        }
        // Remove from the searching set so they can search again
        Indicator.getInstance().getSearchManager().removeSearch(player.getUniqueId());
      }
    }
  }
}
