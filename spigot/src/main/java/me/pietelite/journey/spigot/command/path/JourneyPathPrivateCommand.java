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

package me.pietelite.journey.spigot.command.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.pietelite.journey.common.data.DataAccessException;
import me.pietelite.journey.common.tools.BufferedFunction;
import me.pietelite.journey.spigot.api.navigation.Cell;
import me.pietelite.journey.spigot.command.JourneyCommand;
import me.pietelite.journey.spigot.command.common.CommandNode;
import me.pietelite.journey.spigot.command.common.Parameter;
import me.pietelite.journey.spigot.command.common.PlayerCommandNode;
import me.pietelite.journey.spigot.util.Permissions;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * A command that allows the player to journey to a personal destination.
 */
public class JourneyPathPrivateCommand extends PlayerCommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent
   */
  public JourneyPathPrivateCommand(@NotNull CommandNode parent) {
    super(parent,
        Permissions.JOURNEY_PATH_PRIVATE,
        "Blaze a trail to a private destination",
        "private");

    BufferedFunction<Player, List<String>> customLocationsFunction =
        JourneyCommand.bufferedPersonalEndpointFunction();
    addSubcommand(Parameter.builder()
        .supplier(Parameter.ParameterSupplier.builder()
            .usage("<name>")
            .allowedEntries((src, prev) -> {
              if (src instanceof Player) {
                return customLocationsFunction.apply((Player) src);
              } else {
                return new ArrayList<>();
              }
            }).build())
        .build(), "Use a previously saved custom location");

  }

  @Override
  public boolean onWrappedPlayerCommand(@NotNull Player player,
                                        @NotNull Command command,
                                        @NotNull String label,
                                        @NotNull String[] args,
                                        @NotNull Map<String, String> flags) throws DataAccessException {

    Cell endLocation;

    if (args.length == 0) {
      sendCommandUsageError(player, CommandError.FEW_ARGUMENTS);
      return false;
    }

    PersonalEndpointManager personalEndpointManager =
        ProxyProvider.getDataManager()
        .getPersonalEndpointManager();
    try {
      endLocation = personalEndpointManager.getPersonalEndpoint(player.getUniqueId(), args[0]);

      if (endLocation == null) {
        player.spigot().sendMessage(Format.error("The custom location ",
            Format.toPlain(Format.note(args[0])),
            " could not be found."));
        return false;
      }
    } catch (IllegalArgumentException e) {
      player.spigot().sendMessage(Format.error("Your numbers could not be read."));
      return false;
    }

    int algorithmStepDelay = 0;
    if (CommandFlags.ANIMATE.isIn(flags)) {
      algorithmStepDelay = CommandFlags.ANIMATE.retrieve(player, flags);
    }

    PlayerDestinationGoalSearchSession session = new PlayerDestinationGoalSearchSession(player,
        SpigotUtil.cell(player.getLocation()),
        endLocation,
        CommandFlags.ANIMATE.isIn(flags),
        Settings.DEFAULT_NOFLY_FLAG.getValue() != CommandFlags.NOFLY.isIn(flags),
        Settings.DEFAULT_NODOOR_FLAG.getValue() != CommandFlags.NODOOR.isIn(flags),
        CommandFlags.DIG.isIn(flags),
        algorithmStepDelay);

    int timeout = CommandFlags.TIMEOUT.isIn(flags)
        ? CommandFlags.TIMEOUT.retrieve(player, flags)
        : Settings.DEFAULT_SEARCH_TIMEOUT.getValue();

    session.launchSession(timeout);

    // Check if we should save a custom endpoint
    if (args.length >= 5) {
      if (personalEndpointManager.hasPersonalEndpoint(player.getUniqueId(), endLocation)) {
        player.spigot().sendMessage(Format.error("A custom location already exists at that location!"));
        return false;
      }
      if (personalEndpointManager.hasPersonalEndpoint(player.getUniqueId(), args[4])) {
        player.spigot().sendMessage(Format.error("A custom location already exists with that name!"));
        return false;
      }
      if (Validator.isInvalidDataName(args[5])) {
        player.spigot().sendMessage(Format.error("Your custom name ",
            Format.toPlain(Format.note(args[4])),
            " contains illegal characters."));
        return false;
      }
      // Save it!
      personalEndpointManager.addPersonalEndpoint(player.getUniqueId(), endLocation, args[4]);
      player.spigot().sendMessage(Format.success("Saved your custom location with name ",
          Format.toPlain(Format.note(args[4])), "!"));
    }

    return true;

  }
}