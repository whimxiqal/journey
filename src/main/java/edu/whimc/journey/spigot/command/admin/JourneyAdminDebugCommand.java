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

package edu.whimc.journey.spigot.command.admin;

import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.command.common.CommandError;
import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.manager.DebugManager;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.journey.spigot.util.Permissions;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A command to enable debugging mode.
 */
public class JourneyAdminDebugCommand extends CommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent command
   */
  public JourneyAdminDebugCommand(@Nullable CommandNode parent) {
    super(parent,
        Permissions.ADMIN,
        "Enable or disable debug mode",
        "debug");
    setCanBypassInvalid(true);
  }

  @Override
  public boolean onWrappedCommand(@NotNull CommandSender sender,
                                  @NotNull Command command,
                                  @NotNull String label,
                                  @NotNull String[] args,
                                  @NotNull Map<String, String> flags) {
    boolean enabled;
    if (!(sender instanceof Player player)) {
      if (sender instanceof ConsoleCommandSender) {
        if (JourneySpigot.getInstance()
            .getDebugManager()
            .isConsoleDebugging()) {
          JourneySpigot.getInstance().getDebugManager().setConsoleDebugging(false);
          enabled = false;
        } else {
          JourneySpigot.getInstance().getDebugManager().setConsoleDebugging(true);
          enabled = true;
        }
      } else {
        sendCommandUsageError(sender, CommandError.NO_PLAYER);
        return false;
      }
    } else {

      DebugManager debugManager = JourneySpigot.getInstance().getDebugManager();
      if (debugManager.isDebugging(player.getUniqueId())) {
        debugManager.stopDebugging(player.getUniqueId());
        enabled = false;
      } else {
        debugManager.startDebugging(player.getUniqueId());
        enabled = true;
      }
    }

    if (enabled) {
      sender.spigot().sendMessage(Format.success("Debug mode " + ChatColor.BLUE + "enabled."));
    } else {
      sender.spigot().sendMessage(Format.success("Debug mode " + ChatColor.RED + "disabled."));
    }
    return true;
  }
}
