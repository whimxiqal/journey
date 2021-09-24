package edu.whimc.indicator.spigot.search.tracker;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.navigation.ModeType;
import edu.whimc.indicator.common.navigation.Step;
import edu.whimc.indicator.common.search.Search;
import edu.whimc.indicator.common.search.tracker.SearchAnimator;
import edu.whimc.indicator.common.search.tracker.SearchTracker;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpigotSearchAnimator extends SearchAnimator<LocationCell, World> {

  private final Set<LocationCell> successfulLocations = ConcurrentHashMap.newKeySet();
  private LocationCell lastFailure;
  private final UUID playerUuid;

  public SpigotSearchAnimator(UUID playerUuid, int delayMillis) {
    super(delayMillis);
    this.playerUuid = playerUuid;
  }

  @Override
  protected boolean showResult(LocationCell cell, SearchTracker.Result result, ModeType modeType) {
    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null) {
      return false;
    }

    if (this.successfulLocations.contains(cell)) {
      return false;
    }

    if (player.getLocation().equals(cell.getBlock().getLocation())
        || player.getLocation().add(0, 1, 0).equals(cell.getBlock().getLocation())) {
      return false;
    }

    switch (result) {
      case FAILURE:
        if (lastFailure != null) {
          if (!this.successfulLocations.contains(lastFailure)) {
            hideResult(lastFailure);
          }
        }
        lastFailure = cell;
        return showBlock(player, cell, Material.GLOWSTONE.createBlockData());
      case SUCCESS:
        if (showBlock(player, cell, Material.LIME_STAINED_GLASS.createBlockData())) {
          this.successfulLocations.add(cell);
          return true;
        }
      default:
        Indicator.getInstance().getLogger().info("Unhandled result type in SpigotSearchAnimator");
    }
    return false;
  }

  private void hideResult(LocationCell cell) {
    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null) {
      return;
    }
    player.sendBlockChange(cell.getBlock().getLocation(), cell.getBlock().getBlockData());
    successfulLocations.remove(cell);
  }

  private boolean showBlock(Player player, LocationCell cell, BlockData blockData) {
    if (cell.getDomain() != player.getWorld() || cell.distanceToSquared(new LocationCell(player.getLocation())) > 10000) {
      return false;
    }
    player.sendBlockChange(cell.getBlock().getLocation(), blockData);
    return true;
  }

  private void cleanUpAnimation() {
    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null) {
      return;
    }

    successfulLocations.forEach(cell -> showBlock(player, cell, cell.getBlock().getBlockData()));

    if (lastFailure != null) {
      showBlock(player, lastFailure, lastFailure.getBlock().getBlockData());
    }
  }

  @Override
  public void trailSearchStep(Step<LocationCell, World> step) {
    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null) {
      return;
    }
    showBlock(player, step.getLocatable(), Material.OBSIDIAN.createBlockData());
  }

  @Override
  public void completeTrailSearch(LocationCell origin, LocationCell destination, double distance) {
    cleanUpAnimation();
  }

  @Override
  public void memoryCapacityReached(LocationCell origin, LocationCell destination) {
    cleanUpAnimation();
  }

  @Override
  public void searchStopped(Search<LocationCell, World> search) {
    cleanUpAnimation();
    setAnimating(false);
  }

}
