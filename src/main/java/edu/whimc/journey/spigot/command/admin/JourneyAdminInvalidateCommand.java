package edu.whimc.journey.spigot.command.admin;

import edu.whimc.journey.common.JourneyCommon;
import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.journey.spigot.util.Permissions;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JourneyAdminInvalidateCommand extends CommandNode {

  public JourneyAdminInvalidateCommand(@Nullable CommandNode parent) {
    super(parent, Permissions.ADMIN,
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
