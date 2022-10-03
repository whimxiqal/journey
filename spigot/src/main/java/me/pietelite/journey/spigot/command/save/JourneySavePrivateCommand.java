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

package me.pietelite.journey.spigot.command.save;

import me.pietelite.journey.common.data.DataAccessException;
import me.pietelite.journey.common.data.PersonalEndpointManager;
import me.pietelite.journey.common.util.Validator;
import me.pietelite.journey.spigot.api.navigation.Cell;
import me.pietelite.journey.spigot.command.common.CommandError;
import me.pietelite.journey.spigot.command.common.CommandNode;
import me.pietelite.journey.spigot.command.common.Parameter;
import me.pietelite.journey.spigot.command.common.PlayerCommandNode;
import me.pietelite.journey.spigot.util.Format;
import me.pietelite.journey.spigot.util.Permissions;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A command to allow saving a new personal search endpoint.
 */
public class JourneySavePrivateCommand extends PlayerCommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent command
   */
  public JourneySavePrivateCommand(@Nullable CommandNode parent) {
    super(parent,
        Permissions.JOURNEY_PATH_PRIVATE,
        "Save your current location as a personal path destination",
        "private");

    addSubcommand(Parameter.builder()
        .supplier(Parameter.ParameterSupplier.builder()
            .usage("<name>")
            .build())
        .build(), "Save with this name");
  }

  @Override
  public boolean onWrappedPlayerCommand(@NotNull Player player,
                                        @NotNull Command command,
                                        @NotNull String label,
                                        @NotNull String[] args,
                                        @NotNull Map<String, String> flags) throws DataAccessException {

    if (args.length == 0) {
      sendCommandUsageError(player, CommandError.FEW_ARGUMENTS);
      return false;
    }

    String name = args[0];
    if (Validator.isInvalidDataName(name)) {
      player.spigot().sendMessage(Format.error("That name is invalid."));
      return false;
    }

    PersonalEndpointManager personalEndpointManager =
        ProxyProvider.getDataManager()
        .getPersonalEndpointManager();

    String existingName = personalEndpointManager.getPersonalEndpointName(player.getUniqueId(),
        SpigotUtil.cell(player.getLocation()));
    if (existingName != null) {
      player.spigot().sendMessage(Format.error("Custom location ",
          Format.toPlain(Format.note(existingName)),
          " already exists at that location!"));
      return false;
    }

    Cell existingCell = personalEndpointManager.getPersonalEndpoint(player.getUniqueId(), name);
    if (existingCell != null) {
      player.spigot().sendMessage(Format.error("A custom location already exists with that name at",
          Format.toPlain(Format.locationCell(existingCell, Format.DEFAULT)),
          "!"));
      return false;
    }

    personalEndpointManager.addPersonalEndpoint(player.getUniqueId(),
        SpigotUtil.cell(player.getLocation()), name);
    player.spigot().sendMessage(Format.success("Added custom location named ",
        Format.toPlain(Format.note(name)),
        "."));
    return true;
  }
}