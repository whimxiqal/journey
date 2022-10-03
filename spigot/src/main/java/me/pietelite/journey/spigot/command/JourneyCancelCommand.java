/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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

package me.pietelite.journey.spigot.command;

import java.util.Map;
import java.util.Objects;
import me.pietelite.journey.common.search.SearchSession;
import me.pietelite.journey.spigot.JourneySpigot;
import me.pietelite.journey.spigot.command.common.CommandNode;
import me.pietelite.journey.spigot.command.common.PlayerCommandNode;
import me.pietelite.journey.spigot.search.PlayerDestinationGoalSearchSession;
import me.pietelite.journey.spigot.util.Format;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A command to cancel an ongoing {@link PlayerDestinationGoalSearchSession}.
 */
public class JourneyCancelCommand extends PlayerCommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent command
   */
  public JourneyCancelCommand(@Nullable CommandNode parent) {
    super(parent, null,
        "Cancel the current search", "cancel");
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

    SearchSession session = Objects.requireNonNull(JourneySpigot.getInstance()
        .getSearchManager()
        .getSearch(player.getUniqueId()));

    if (session.stop()) {
      player.spigot().sendMessage(Format.success("Search cancelling..."));
      return true;
    } else {
      player.spigot().sendMessage(Format.error("You do not have an ongoing search."));
      return false;
    }

  }

}
