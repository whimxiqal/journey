package edu.whimc.indicator.spigot.search.tracker;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.path.Completion;
import edu.whimc.indicator.common.path.Path;
import edu.whimc.indicator.common.path.Step;
import edu.whimc.indicator.common.search.tracker.SearchAnimator;
import edu.whimc.indicator.common.search.tracker.SearchTracker;
import edu.whimc.indicator.spigot.path.LocationCell;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpigotSearchAnimator extends SearchAnimator<LocationCell, World> {

  private final Set<LocationCell> successfulLocations = ConcurrentHashMap.newKeySet();
  private final UUID playerUuid;

  public SpigotSearchAnimator(UUID playerUuid, int delayMillis) {
    super(delayMillis);
    this.playerUuid = playerUuid;
  }

  @Override
  protected boolean showResult(LocationCell cell, SearchTracker.Result result) {
    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null) {
      return false;
    }

    if (player.getLocation().equals(cell.getBlock().getLocation())
        || player.getLocation().add(0, 1, 0).equals(cell.getBlock().getLocation())) {
      return false;
    }

    if (cell.getDomain() != player.getWorld() || cell.distanceToSquared(new LocationCell(player.getLocation())) > 10000) {
      return false;
    }

    switch (result) {
      case FAILURE:
        if (!this.successfulLocations.contains(cell)) {
          player.sendBlockChange(cell.getBlock().getLocation(), Material.REDSTONE_BLOCK.createBlockData());
          Bukkit.getScheduler().runTaskLater(Indicator.getInstance(), () -> hideResult(cell), 20 /* ticks per second */ * 2 /* seconds */);
          return true;
        } else {
          return false;
        }
      case SUCCESS:
        if (!this.successfulLocations.contains(cell)) {
          player.sendBlockChange(cell.getBlock().getLocation(), Material.EMERALD_BLOCK.createBlockData());
          this.successfulLocations.add(cell);
          Bukkit.getScheduler().runTaskLater(Indicator.getInstance(), () -> hideResult(cell), 20 /* ticks per second */ * 10 /* seconds */);
          return true;
        } else {
          return false;
        }
      default:
        Indicator.getInstance().getLogger().info("Unhandled result type in SpigotSearchAnimator");
        return false;
    }
  }

  private void hideResult(LocationCell cell) {
    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null) {
      return;
    }
    player.sendBlockChange(cell.getBlock().getLocation(), cell.getBlock().getBlockData());
    successfulLocations.remove(cell);
  }

  @Override
  public void foundNewOptimalPath(Path<LocationCell, World> path, Completion<LocationCell, World> completion) {

  }

  @Override
  public void startTrailSearch(LocationCell origin, LocationCell destination) {

  }

  @Override
  public void trailSearchVisitation(Step<LocationCell, World> step) {

  }

  @Override
  public void trailSearchStep(Step<LocationCell, World> step) {

  }

  @Override
  public void finishTrailSearch(LocationCell origin, LocationCell destination, double distance) {

  }

  @Override
  public void memoryCapacityReached(LocationCell origin, LocationCell destination) {

  }
}
