package edu.whimc.indicator.spigot.command.common;

import edu.whimc.indicator.common.data.DataAccessException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class PlayerCommandNode extends CommandNode {
  public PlayerCommandNode(@Nullable CommandNode parent, @Nullable Permission permission, @NotNull String description, @NotNull String primaryAlias) {
    super(parent, permission, description, primaryAlias);
  }

  public PlayerCommandNode(@Nullable CommandNode parent, @Nullable Permission permission, @NotNull String description, @NotNull String primaryAlias, boolean addHelp) {
    super(parent, permission, description, primaryAlias, addHelp);
  }

  @Override
  public final boolean onWrappedCommand(@NotNull CommandSender sender,
                                  @NotNull Command command,
                                  @NotNull String label,
                                  @NotNull String[] args,
                                  @NotNull Set<String> flags) throws DataAccessException {
    Player player;
    if (!(sender instanceof Player)) {
      sendCommandError(sender, CommandError.ONLY_PLAYER);
      return false;
    }
    player = (Player) sender;
    return onWrappedPlayerCommand(player, command, label, args, flags);
  }

  public abstract boolean onWrappedPlayerCommand(@NotNull Player player,
                                        @NotNull Command command,
                                        @NotNull String label,
                                        @NotNull String[] args,
                                        @NotNull Set<String> flags) throws DataAccessException;
}
