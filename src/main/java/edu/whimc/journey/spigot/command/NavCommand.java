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

package edu.whimc.journey.spigot.command;

import edu.whimc.journey.common.config.Settings;
import edu.whimc.journey.common.navigation.Itinerary;
import edu.whimc.journey.common.search.ResultState;
import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.command.common.CommandFlags;
import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.journey.spigot.command.common.PlayerCommandNode;
import edu.whimc.journey.spigot.journey.PlayerJourney;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.search.PlayerSearchSession;
import edu.whimc.journey.spigot.search.SearchFlag;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.journey.spigot.util.Permissions;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import me.blackvein.quests.Quests;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NavCommand extends FunctionlessCommandNode {

  public NavCommand() {
    super(null,
        Permissions.NAV_USE_PERMISSION,
        "View your current activated trail",
        "nav");
    addChildren(new TrailAcceptCommand(this));
    addChildren(new TrailCancelCommand(this));
    addChildren(new NavCustomCommand(this));
    addChildren(new NavServerCommand(this));
    // Quests plugin
    Plugin questsPlugin = Bukkit.getPluginManager().getPlugin("Quests");
    if (questsPlugin instanceof Quests) {
      addChildren(new NavQuestsCommand(this, (Quests) questsPlugin));
    }
  }

  public static boolean blazeTrailTo(@NotNull Player player,
                                     @NotNull LocationCell endpoint,
                                     @NotNull Map<String, String> flags) {

    UUID playerUuid = player.getUniqueId();

    Set<SearchFlag> searchFlags = new HashSet<>();
    if (Settings.DEFAULT_NOFLY_FLAG.getValue() != CommandFlags.NOFLY.isIn(flags)) {
      searchFlags.add(SearchFlag.NOFLY);
    }
    if (Settings.DEFAULT_NODOOR_FLAG.getValue() != CommandFlags.NODOOR.isIn(flags)) {
      searchFlags.add(SearchFlag.NODOOR);
    }

    int algorithmStepDelay = 0;
    if (CommandFlags.ANIMATE.isIn(flags)) {
      searchFlags.add(SearchFlag.ANIMATE);
      algorithmStepDelay = CommandFlags.ANIMATE.retrieve(player, flags);
    }
    PlayerSearchSession session = new PlayerSearchSession(player, searchFlags, algorithmStepDelay);

    int timeout = CommandFlags.TIMEOUT.isIn(flags)
        ? CommandFlags.TIMEOUT.retrieve(player, flags)
        : Settings.DEFAULT_SEARCH_TIMEOUT.getValue();

    // Set up a "Working..." message if it takes too long
    player.spigot().sendMessage(Format.info("Searching for path to your destination (" + timeout + " sec)..."));
    player.spigot().sendMessage(new ComponentBuilder()
        .append(Format.PREFIX)
        .append(new ComponentBuilder()
            .append("  [Search Flags]")
            .color(Format.ACCENT2.asBungee())
            .italic(true)
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text(new ComponentBuilder("Flags: ")
                    .color(Format.ACCENT2.asBungee())
                    .append(searchFlags.stream()
                        .map(flag -> flag.name().toLowerCase())
                        .collect(Collectors.joining(", ")))
                    .color(Format.INFO.asBungee())
                    .create())))
            .create())
        .create());

    // Set up a search cancellation in case it takes too long
    BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(JourneySpigot.getInstance(),
        () -> {
          if (session.getState() != ResultState.SUCCESSFUL) {
            player.spigot().sendMessage(Format.error("Time limit surpassed. Canceling search..."));
          }
          session.cancel();
        },
        (long) timeout * 20 /* ticks per second */);

    // SEARCH
    BukkitTask searchTask = Bukkit.getScheduler().runTaskAsynchronously(JourneySpigot.getInstance(), () -> {
          // Search... this may take a long time
          session.search(new LocationCell(player.getLocation()), endpoint);
          // Cancel the timeout message if it hasn't happened yet
          timeoutTask.cancel();
        }
    );

    return true;
  }

  public static class TrailCancelCommand extends PlayerCommandNode {

    public TrailCancelCommand(@Nullable CommandNode parent) {
      super(parent, Permissions.NAV_USE_PERMISSION, "Cancel the current search", "cancel");
    }

    @Override
    public boolean onWrappedPlayerCommand(@NotNull Player player,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args,
                                          @NotNull Map<String, String> flags) {
      if (!JourneySpigot.getInstance().getSearchManager().isSearching(player.getUniqueId())) {
        player.spigot().sendMessage(Format.error("You do not have an ongoing search."));
        return false;
      }

      player.spigot().sendMessage(Format.success("Cancelling search..."));
      Objects.requireNonNull(JourneySpigot.getInstance().getSearchManager().getSearch(player.getUniqueId())).cancel();
      return true;
    }

  }

  public static class TrailAcceptCommand extends PlayerCommandNode {

    public TrailAcceptCommand(@Nullable CommandNode parent) {
      super(parent, Permissions.NAV_USE_PERMISSION, "Accept a new trail suggestion", "accept");
    }

    @Override
    public boolean onWrappedPlayerCommand(@NotNull Player player,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args,
                                          @NotNull Map<String, String> flags) {

      PlayerJourney journey = JourneySpigot.getInstance().getSearchManager().getJourney(player.getUniqueId());
      if (journey == null) {
        player.spigot().sendMessage(Format.error("You have nothing to accept."));
        return false;
      }

      Itinerary<LocationCell, World> prospectiveItinerary = journey.getProspectiveItinerary();
      if (prospectiveItinerary == null) {
        player.spigot().sendMessage(Format.error("You have nothing to accept."));
        return false;
      }

      PlayerJourney newJourney = new PlayerJourney(player.getUniqueId(), journey.getSession(), prospectiveItinerary);
      journey.stop();

      JourneySpigot.getInstance().getSearchManager().putJourney(player.getUniqueId(), newJourney);
      newJourney.illuminateTrail();

      player.spigot().sendMessage(Format.success("Make your way back to your starting point;"));
      player.spigot().sendMessage(Format.success("You have accepted a new trail."));

      return true;
    }

  }

}
