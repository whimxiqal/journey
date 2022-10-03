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

import me.pietelite.journey.common.navigation.Itinerary;
import me.pietelite.journey.spigot.JourneySpigot;
import me.pietelite.journey.spigot.command.common.CommandNode;
import me.pietelite.journey.spigot.command.common.PlayerCommandNode;
import me.pietelite.journey.common.navigation.PlayerJourneySession;
import me.pietelite.journey.spigot.search.PlayerDestinationGoalSearchSession;
import me.pietelite.journey.spigot.util.Format;
import java.util.Map;
import java.util.Objects;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A command to allow players to accept a new journey from
 * an ongoing {@link PlayerDestinationGoalSearchSession}.
 */
public class JourneyAcceptCommand extends PlayerCommandNode {

  /**
   * General constructor.
   *
   * @param parent parent command
   */
  public JourneyAcceptCommand(@Nullable CommandNode parent) {
    super(parent, null,
        "Accept a new trail suggestion", "accept");
  }

  @Override
  public boolean onWrappedPlayerCommand(@NotNull Player player,
                                        @NotNull Command command,
                                        @NotNull String label,
                                        @NotNull String[] args,
                                        @NotNull Map<String, String> flags) {
    if (!JourneySpigot.getInstance().getSearchManager().hasJourney(player.getUniqueId())) {
      player.spigot().sendMessage(Format.error("You have nothing to accept."));
      return false;
    }

    PlayerJourneySession journey = Objects.requireNonNull(JourneySpigot.getInstance()
        .getSearchManager()
        .getJourney(player.getUniqueId()));

    Itinerary prospectivePath = journey.getProspectiveItinerary();
    if (prospectivePath == null) {
      player.spigot().sendMessage(Format.error("You have nothing to accept."));
      return false;
    }

    PlayerJourneySession newJourney = new PlayerJourneySession(player.getUniqueId(), journey.getSession(), prospectivePath);
    journey.stop();

    JourneySpigot.getInstance().getSearchManager().putJourney(player.getUniqueId(), newJourney);
    newJourney.run();

    player.spigot().sendMessage(Format.success("Make your way back to your starting point;"));
    player.spigot().sendMessage(Format.success("You have accepted a new trail."));

    return true;
  }

}
