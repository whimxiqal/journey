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
import edu.whimc.indicator.spigot.command.common.CommandNode;
import edu.whimc.indicator.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.indicator.spigot.search.IndicatorSearch;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.search.tracker.SpigotCompleteSearchTracker;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.indicator.spigot.util.Permissions;
import me.blackvein.quests.Quests;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class TrailCommand extends FunctionlessCommandNode {

  private static final List<CommandNode> extraChildren = new LinkedList<>();

  public TrailCommand() {
    super(null,
        Permissions.TRAIL_BLAZE_PERMISSION,
        "View your current activated trail",
        "trail");
    addChildren(new TrailCustomCommand(this));
    addChildren(new TrailServerCommand(this));
    // Quests plugin
    Plugin questsPlugin = Bukkit.getPluginManager().getPlugin("Quests");
    if (questsPlugin instanceof Quests) {
      addChildren(new TrailQuestsCommand(this, (Quests) questsPlugin));
    }
    // Add extra children
    addChildren(extraChildren.toArray(new CommandNode[0]));
  }

  public static boolean blazeTrailTo(@NotNull Player player,
                                     @NotNull LocationCell endpoint,
                                     @NotNull Set<String> flags,
                                     int timeout) {

    if (Indicator.getInstance().getJourneyManager().isSearching(player.getUniqueId())) {
      player.spigot().sendMessage(Format.error("Please wait until your search is over before performing a new one."));
      return false;
    }

    UUID playerUuid = player.getUniqueId();

    IndicatorSearch search = new IndicatorSearch(player, flags.contains("nofly"));

    // Set up a search cancellation in case it takes too long
    BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(Indicator.getInstance(),
        search::cancel,
        (long) timeout * 20 /* ticks per second */);

    // Set up a "Working..." message if it takes too long
    player.spigot().sendMessage(Format.info("Searching for path to your destination (" + timeout + " sec)..."));

    // Build tracker
    SpigotCompleteSearchTracker.Builder trackerBuilder = SpigotCompleteSearchTracker.builder(player);
    if (flags.contains("animate")) {
      trackerBuilder.animate(10);
    }

    // SEARCH
    Bukkit.getScheduler().runTaskAsynchronously(Indicator.getInstance(), () -> {
          // Add to the searching set so they can't search again
          Indicator.getInstance().getJourneyManager().startSearching(player.getUniqueId());
          // Search... this may take a long time
          search.search(new LocationCell(player.getLocation()), endpoint, trackerBuilder.build());

          // Cancel the timeout message if it hasn't happened yet
          timeoutTask.cancel();
          // Send failure message if we finished unsuccessfully
          if (!search.isSuccessful()) {
            player.spigot().sendMessage(Format.error("A path to your destination could not be found."));
            Indicator.getInstance().getJourneyManager().removePlayerJourney(playerUuid);
          }
          // Remove from the searching set so they can search again
          Indicator.getInstance().getJourneyManager().stopSearching(player.getUniqueId());
        }
    );

    return true;
  }

  public static void registerChild(CommandNode commandNode) {
    extraChildren.add(commandNode);
  }

  public static int getTimeout(CommandSender src, String[] args, int timeoutIndex) {
    if (timeoutIndex >= args.length) {
      return Settings.DEFAULT_SEARCH_TIMEOUT.getValue();
    }
    int timeout;
    try {
      timeout = Integer.parseInt(args[timeoutIndex]);
    } catch (NumberFormatException e) {
      src.spigot().sendMessage(Format.error("The timeout could not be converted to an integer"));
      return Settings.DEFAULT_SEARCH_TIMEOUT.getValue();
    }

    if (timeout < 1) {
      src.spigot().sendMessage(Format.error("The timeout must be at least 1 second"));
      return Settings.DEFAULT_SEARCH_TIMEOUT.getValue();
    } else if (timeout > 600) {
      src.spigot().sendMessage(Format.error("The timeout must be at most 10 minutes"));
      return Settings.DEFAULT_SEARCH_TIMEOUT.getValue();
    }
    return timeout;
  }

}
