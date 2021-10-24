package edu.whimc.journey.spigot.command;

import edu.whimc.journey.common.JourneyCommon;
import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.journey.spigot.util.Permissions;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JourneyInvalidateCommand extends CommandNode {

  public JourneyInvalidateCommand(@Nullable CommandNode parent) {
    super(parent, Permissions.ADMIN_PERMISSION,
        "Clear the internal Journey cache",
        "invalidate");
  }

  @Override
  public boolean onWrappedCommand(@NotNull CommandSender sender,
                                  @NotNull Command command,
                                  @NotNull String label,
                                  @NotNull String[] args,
                                  @NotNull Map<String, String> flags) {
    JourneyCommon.getPathCache().clear();
    sender.spigot().sendMessage(Format.success("Cleared cache."));
    return true;
  }
}
