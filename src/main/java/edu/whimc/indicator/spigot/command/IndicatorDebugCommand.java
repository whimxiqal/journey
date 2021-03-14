package edu.whimc.indicator.spigot.command;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.spigot.cache.DebugManager;
import edu.whimc.indicator.spigot.command.common.CommandNode;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.indicator.spigot.util.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IndicatorDebugCommand extends CommandNode {

  public IndicatorDebugCommand(@Nullable CommandNode parent) {
    super(parent,
        Permissions.DEBUG_PERMISSION,
        "Enable or disable debug mode",
        "debug");
  }

  @Override
  public boolean onWrappedCommand(@NotNull CommandSender sender,
                                  @NotNull Command command,
                                  @NotNull String label,
                                  @NotNull String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(Format.error("Only players may execute this command"));
      return false;
    }
    Player player = (Player) sender;

    DebugManager debugManager = Indicator.getInstance().getDebugManager();
    if (debugManager.isDebugging(player.getUniqueId())) {
      debugManager.stopDebugging(player.getUniqueId());
      player.sendMessage(Format.success("Debug mode " + ChatColor.RED + "disabled"));
    } else {
      debugManager.startDebugging(player.getUniqueId());
      player.sendMessage(Format.success("Debug mode " + ChatColor.BLUE + "enabled"));
    }
    return true;
  }
}
