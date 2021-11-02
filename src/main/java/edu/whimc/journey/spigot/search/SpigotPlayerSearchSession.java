/*
 * MIT License
 *
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
 *
 */

package edu.whimc.journey.spigot.search;

import edu.whimc.journey.common.search.SearchSession;
import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.journey.spigot.util.TimeUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface SpigotPlayerSearchSession<S extends SearchSession<LocationCell, World>> {

  @Nullable
  Player getPlayer();

  AnimationManager getAnimationManager();

  PlayerSessionState getSessionState();

  S getSession();

  default void launchSession(int timeout) {

    Player player = getPlayer();
    if (player == null) {
      return;
    }

    // Set up a "Working..." message if it takes too long
    player.spigot().sendMessage(new ComponentBuilder()
        .append(Format.PREFIX)
        .append(new ComponentBuilder()
            .append("Searching...")
            .color(Format.INFO.asBungee())
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text(new ComponentBuilder("Search Parameters")
                    .color(Format.THEME.asBungee())
                    .bold(true)
                    .create()),
                new Text(new ComponentBuilder("\nTimeout: ")
                    .bold(false)
                    .color(ChatColor.DARK_GRAY.asBungee())
                    .append(TimeUtil.toSimpleTime(timeout))
                    .color(Format.ACCENT2.asBungee())
                    .create())))
            .create())
        .create());
    // Don't put anything after the search message...
    //    player.spigot().sendMessage(new ComponentBuilder()
    //        .append(Format.PREFIX)
    //        .append(new ComponentBuilder()
    //            .append("  [Search Flags]")
    //            .color(Format.ACCENT2.asBungee())
    //            .italic(true)
    //            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
    //                new Text(Format.note("Unimplemented"))))
    //            .create())
    //        .create());

    // Set up a search cancellation in case it takes too long
    Bukkit.getScheduler().runTaskLater(JourneySpigot.getInstance(),
        () -> {
          if (getSession().stop()) {
            player.spigot().sendMessage(Format.error("Time limit surpassed. Canceling search..."));
          }
        },
        (long) timeout * 20 /* ticks per second */);

    // SEARCH
    Bukkit.getScheduler().runTaskAsynchronously(JourneySpigot.getInstance(), () -> {
          // Search... this may take a long time
          getSession().search();
        }
    );
  }

}
