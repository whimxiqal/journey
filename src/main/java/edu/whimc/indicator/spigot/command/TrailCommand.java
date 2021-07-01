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

package edu.whimc.indicator.spigot.command;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.config.Settings;
import edu.whimc.indicator.common.path.Path;
import edu.whimc.indicator.common.search.tracker.SearchTrackerCollection;
import edu.whimc.indicator.spigot.command.common.CommandNode;
import edu.whimc.indicator.spigot.command.common.Flags;
import edu.whimc.indicator.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.indicator.spigot.command.common.PlayerCommandNode;
import edu.whimc.indicator.spigot.journey.PlayerJourney;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.search.IndicatorSearch;
import edu.whimc.indicator.spigot.search.tracker.SpigotSearchAnimator;
import edu.whimc.indicator.spigot.search.tracker.SpigotSearchTracker;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.indicator.spigot.util.Permissions;
import me.blackvein.quests.Quests;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public final class TrailCommand extends FunctionlessCommandNode {

  public TrailCommand() {
    super(null,
        Permissions.TRAIL_USE_PERMISSION,
        "View your current activated trail",
        "trail");
    addChildren(new TrailAcceptCommand(this));
    addChildren(new TrailCancelCommand(this));
    addChildren(new TrailCustomCommand(this));
    addChildren(new TrailServerCommand(this));
    // Quests plugin
    Plugin questsPlugin = Bukkit.getPluginManager().getPlugin("Quests");
    if (questsPlugin instanceof Quests) {
      addChildren(new TrailQuestsCommand(this, (Quests) questsPlugin));
    }
  }

  public static boolean blazeTrailTo(@NotNull Player player,
                                     @NotNull LocationCell endpoint,
                                     @NotNull Map<String, String> flags) {

    if (Indicator.getInstance().getSearchManager().isSearching(player.getUniqueId())) {
      player.spigot().sendMessage(Format.error("Please wait until your search is over before performing a new one."));
      return false;
    }

    UUID playerUuid = player.getUniqueId();

    IndicatorSearch search = new IndicatorSearch(player, Flags.NOFLY.isIn(flags));

    int timeout = Flags.TIMEOUT.isIn(flags)
        ? Flags.TIMEOUT.retrieve(player, flags)
        : Settings.DEFAULT_SEARCH_TIMEOUT.getValue();

    // Set up a "Working..." message if it takes too long
    player.spigot().sendMessage(Format.info("Searching for path to your destination (" + timeout + " sec)..."));
    player.spigot().sendMessage(Format.info(" " + Format.toPlain(Format.locationCell(endpoint, Format.INFO)) + " !"));

    // Build tracker collection
    SearchTrackerCollection<LocationCell, World> trackerCollection = new SearchTrackerCollection<>();
    trackerCollection.addTracker(new SpigotSearchTracker(playerUuid));
    if (Flags.ANIMATE.isIn(flags)) {
      trackerCollection.addTracker(new SpigotSearchAnimator(playerUuid, Flags.ANIMATE.retrieve(player, flags)));
    }

    search.setTracker(trackerCollection);

    // Set up a search cancellation in case it takes too long
    BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(Indicator.getInstance(),
        () -> {
          if (search.cancel()) {
            player.spigot().sendMessage(Format.chain(Format.error("Your search took too long. "
                + "Try extending the timeout value with the \"-timeout:<seconds>\" flag")));
          }
        },
        (long) timeout * 20 /* ticks per second */);

    // SEARCH
    BukkitTask searchTask = Bukkit.getScheduler().runTaskAsynchronously(Indicator.getInstance(), () -> {
          // Search... this may take a long time
          search.search(new LocationCell(player.getLocation()), endpoint);
          // Cancel the timeout message if it hasn't happened yet
          timeoutTask.cancel();
        }
    );

    return true;
  }

  public static class TrailCancelCommand extends PlayerCommandNode {

    public TrailCancelCommand(@Nullable CommandNode parent) {
      super(parent, Permissions.TRAIL_USE_PERMISSION, "Cancel the current search", "cancel");
    }

    @Override
    public boolean onWrappedPlayerCommand(@NotNull Player player,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args,
                                          @NotNull Map<String, String> flags) {
      if (!Indicator.getInstance().getSearchManager().isSearching(player.getUniqueId())) {
        player.spigot().sendMessage(Format.error("You do not have an ongoing search"));
        return false;
      }

      Indicator.getInstance().getSearchManager().getSearch(player.getUniqueId()).cancel();
      player.spigot().sendMessage(Format.success("Search canceled."));
      return true;
    }

  }

  public static class TrailAcceptCommand extends PlayerCommandNode {

    public TrailAcceptCommand(@Nullable CommandNode parent) {
      super(parent, Permissions.TRAIL_USE_PERMISSION, "Accept a new trail suggestion", "accept");
    }

    @Override
    public boolean onWrappedPlayerCommand(@NotNull Player player,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args,
                                          @NotNull Map<String, String> flags) {
      if (!Indicator.getInstance().getSearchManager().hasPlayerJourney(player.getUniqueId())) {
        player.spigot().sendMessage(Format.error("You have nothing to accept."));
        return false;
      }

      PlayerJourney journey = Indicator.getInstance().getSearchManager().getPlayerJourney(player.getUniqueId());

      Path<LocationCell, World> prospectivePath = journey.getProspectivePath();
      if (prospectivePath == null) {
        player.spigot().sendMessage(Format.error("You have nothing to accept."));
        return false;
      }

      PlayerJourney newJourney = new PlayerJourney(player.getUniqueId(), prospectivePath);
      journey.stop();

      Indicator.getInstance().getSearchManager().putPlayerJourney(player.getUniqueId(), newJourney);
      newJourney.illuminateTrail();

      player.spigot().sendMessage(Format.success("Make your way back to your starting point;"));
      player.spigot().sendMessage(Format.success("You have accepted a new trail."));

      return true;
    }

  }

}
