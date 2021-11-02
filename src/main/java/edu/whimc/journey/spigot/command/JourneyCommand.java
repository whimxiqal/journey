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
import edu.whimc.journey.common.data.DataAccessException;
import edu.whimc.journey.common.search.ResultState;
import edu.whimc.journey.common.tools.BufferedFunction;
import edu.whimc.journey.common.tools.BufferedSupplier;
import edu.whimc.journey.common.util.Extra;
import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.command.common.CommandFlags;
import edu.whimc.journey.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.search.PlayerDestinationGoalSearchSession;
import edu.whimc.journey.spigot.search.SearchFlag;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.journey.spigot.util.Permissions;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Root command of all commands for the Journey plugin.
 */
public class JourneyCommand extends FunctionlessCommandNode {

  /**
   * General constructor.
   */
  public JourneyCommand() {
    super(null, Permissions.JOURNEY_USE,
        "The root for all journey commands",
        "journey");
    addChildren(new JourneyAcceptCommand(this));
    addChildren(new JourneyAdminCommand(this));
    addChildren(new JourneyCancelCommand(this));
    addChildren(new JourneyDeleteCommand(this));
    addChildren(new JourneyListCommand(this));
    addChildren(new JourneySaveCommand(this));
    addChildren(new JourneyToCommand(this));
    setCanBypassInvalid(true);
  }

  /**
   * Get a buffered supplier of a list of the names of public endpoints,
   * quoted as needed for multi-word names.
   *
   * @return the supplier
   */
  @NotNull
  public static BufferedSupplier<List<String>> bufferedPublicEndpointSupplier() {
    return new BufferedSupplier<>(() -> {
      try {
        return JourneySpigot.getInstance().getDataManager()
            .getPublicEndpointManager()
            .getPublicEndpoints().keySet()
            .stream().map(Extra::quoteStringWithSpaces).collect(Collectors.toList());
      } catch (DataAccessException e) {
        return new LinkedList<>();
      }
    }, 1000);
  }

  /**
   * Get a buffered supplier of a map of players to the names of their personal endpoints,
   * quoted as needed for multi-word names.
   *
   * @return the supplier
   */
  @NotNull
  public static BufferedFunction<Player, List<String>> bufferedPersonalEndpointFunction() {
    return new BufferedFunction<>(player -> {
      try {
        return JourneySpigot.getInstance().getDataManager()
            .getPersonalEndpointManager()
            .getPersonalEndpoints(player.getUniqueId()).keySet()
            .stream().map(Extra::quoteStringWithSpaces).collect(Collectors.toList());
      } catch (DataAccessException e) {
        return new LinkedList<>();
      }
    }, 1000);
  }

}
