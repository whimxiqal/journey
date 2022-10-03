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

package me.pietelite.journey.spigot.command.delete;

import java.util.List;
import java.util.Map;
import me.pietelite.journey.common.data.DataAccessException;
import me.pietelite.journey.common.data.PublicEndpointManager;
import me.pietelite.journey.common.tools.BufferedSupplier;
import me.pietelite.journey.spigot.command.JourneyCommand;
import me.pietelite.journey.spigot.command.common.CommandError;
import me.pietelite.journey.spigot.command.common.CommandNode;
import me.pietelite.journey.spigot.command.common.Parameter;
import me.pietelite.journey.spigot.command.common.PlayerCommandNode;
import me.pietelite.journey.spigot.util.Format;
import me.pietelite.journey.spigot.util.Permissions;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A command to allow someone to delete a public destination.
 */
public class JourneyDeletePublicCommand extends PlayerCommandNode {

  /**
   * General constructor.
   *
   * @param parent the player
   */
  public JourneyDeletePublicCommand(@Nullable CommandNode parent) {
    super(parent, Permissions.JOURNEY_EDIT_PUBLIC,
        "Delete a saved public location",
        "public");

    BufferedSupplier<List<String>> serverLocationSupplier = JourneyCommand.bufferedPublicEndpointSupplier();
    addSubcommand(Parameter.builder()
        .supplier(Parameter.ParameterSupplier.builder()
            .usage("<name>")
            .allowedEntries((src, prev) -> serverLocationSupplier.get())
            .strict(false)
            .build())
        .build(), "Remove a previously saved server location");
  }

  @Override
  public boolean onWrappedPlayerCommand(@NotNull Player player,
                                        @NotNull Command command,
                                        @NotNull String label,
                                        @NotNull String[] args,
                                        @NotNull Map<String, String> flags) throws DataAccessException {
    if (args.length < 1) {
      sendCommandUsageError(player, CommandError.FEW_ARGUMENTS);
      return false;
    }

    PublicEndpointManager endpointManager =
        Journey.get().proxy().dataManager()
            .getPublicEndpointManager();
    if (endpointManager.hasPublicEndpoint(args[0])) {
      Journey.get().proxy().dataManager()
          .getPublicEndpointManager()
          .removePublicEndpoint(args[0]);
      player.spigot().sendMessage(Format.success("The server location ",
          Format.toPlain(Format.note(args[0])),
          " has been removed."));
      return true;
    } else {
      player.spigot().sendMessage(Format.error("The server location ",
          Format.toPlain(Format.note(args[0])),
          " could not be found."));
      return false;
    }
  }
}
