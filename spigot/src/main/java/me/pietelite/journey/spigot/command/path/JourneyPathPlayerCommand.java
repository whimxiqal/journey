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

import java.util.Map;
import java.util.stream.Collectors;
import me.pietelite.journey.common.config.Settings;
import me.pietelite.journey.common.data.DataAccessException;
import me.pietelite.journey.common.search.PlayerDestinationGoalSearchSession;
import me.pietelite.journey.spigot.api.navigation.Cell;
import me.pietelite.journey.spigot.command.common.CommandError;
import me.pietelite.journey.spigot.command.common.CommandFlags;
import me.pietelite.journey.spigot.command.common.CommandNode;
import me.pietelite.journey.spigot.command.common.Parameter;
import me.pietelite.journey.spigot.command.common.PlayerCommandNode;
import me.pietelite.journey.spigot.util.Format;
import me.pietelite.journey.spigot.util.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JourneyPathPlayerCommand extends PlayerCommandNode {

    /**
     * General constructor.
     *
     * @param parent the parent
     */
    public JourneyPathPlayerCommand(@NotNull CommandNode parent) {
        super(parent,
                Permissions.JOURNEY_PATH_PLAYER,
                "Blaze a trail to another player",
                "player");

        addSubcommand(Parameter.builder()
                .supplier(Parameter.ParameterSupplier.builder()
                        .usage("<player>")
                        .allowedEntries((src, prev) ->
                            Bukkit.getServer().getOnlinePlayers().stream()
                                    .map(HumanEntity::getName)
                                    .collect(Collectors.toList())).build())
                .build(), "Go to another player");
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

        Player destination = Bukkit.getPlayer(args[0]);
        if (destination == null) {
            player.spigot().sendMessage(Format.error("That player doesn't exist or isn't online"));
            return false;
        }

        int algorithmStepDelay = 0;
        if (CommandFlags.ANIMATE.isIn(flags)) {
            algorithmStepDelay = CommandFlags.ANIMATE.retrieve(player, flags);
        }

        PlayerDestinationGoalSearchSession session = new PlayerDestinationGoalSearchSession(player,
                SpigotUtil.cell(player.getLocation()),
                SpigotUtil.cell(destination.getLocation()),
                CommandFlags.ANIMATE.isIn(flags),
                Settings.DEFAULT_NOFLY_FLAG.getValue() != CommandFlags.NOFLY.isIn(flags),
                Settings.DEFAULT_NODOOR_FLAG.getValue() != CommandFlags.NODOOR.isIn(flags),
                CommandFlags.DIG.isIn(flags),
                algorithmStepDelay);

        int timeout = CommandFlags.TIMEOUT.isIn(flags)
                ? CommandFlags.TIMEOUT.retrieve(player, flags)
                : Settings.DEFAULT_SEARCH_TIMEOUT.getValue();

        session.launchSession(timeout);
        return true;
    }
}
