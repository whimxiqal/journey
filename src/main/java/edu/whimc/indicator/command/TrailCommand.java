package edu.whimc.indicator.command;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.command.common.CommandNode;
import edu.whimc.indicator.destination.IndicatorDestination;
import edu.whimc.indicator.search.IndicatorSearch;
import edu.whimc.indicator.search.LocationLocatable;
import edu.whimc.indicator.util.Format;
import edu.whimc.indicator.util.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class TrailCommand extends CommandNode {
  public TrailCommand() {
    super(null,
        Permissions.TRAIL_PERMISSION,
        "Provide a trail to your destination",
        "trail");
  }

  @Override
  public boolean onWrappedCommand(@NotNull CommandSender sender,
                                  @NotNull Command command,
                                  @NotNull String label,
                                  @NotNull String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(Format.error("Only players may perform this command"));
      return false;
    }
    Player player = (Player) sender;
    IndicatorDestination destination;
    if (Indicator.getInstance()
        .getDestinationManager()
        .containsKey(player.getUniqueId())) {
      destination = Indicator.getInstance().getDestinationManager().get(player.getUniqueId());
    } else {
      sender.sendMessage(Format.warn("You don't have a destination set!"));
      sender.sendMessage(Format.warn("Defaulting to your world's spawn."));
      destination = new IndicatorDestination(Indicator.getInstance(), new LocationLocatable(player.getWorld().getSpawnLocation()));
      Indicator.getInstance().getDestinationManager().put(player.getUniqueId(), destination);
    }

    Bukkit.getScheduler().runTaskAsynchronously(Indicator.getInstance(), () -> {
      IndicatorSearch search = new IndicatorSearch();
      search.setLocalSearchVisitationCallback(loc -> Indicator.getInstance().getLogger().info("Pathfinding - Visited: " + loc.print()));
      List<LocationLocatable> path = search.findPath(
          new LocationLocatable(player.getLocation()),
          destination.getLocation());

      if (path == null) {
        sender.sendMessage(Format.error("A path to your destination could not be found."));
        return;
      }

      sender.sendMessage(Format.success("Showing a trail to your destination"));

      Random rand = new Random();
      BukkitTask illumination = Bukkit.getScheduler().runTaskTimer(Indicator.getInstance(), () -> {
        for (LocationLocatable location : path) {
          for (int i = 0; i < 6; i++) {
            location.getWorld().spawnParticle(Particle.FLAME,
                location.getBlockX() + rand.nextDouble(),
                location.getBlockY() + 0.2f,
                location.getBlockZ() + rand.nextDouble(),
                1,
                0, 0, 0,
                0);
          }
        }
      }, 0, 20);

      Bukkit.getScheduler().runTaskLater(Indicator.getInstance(), illumination::cancel, 100);

    });
    return true;

  }
}
