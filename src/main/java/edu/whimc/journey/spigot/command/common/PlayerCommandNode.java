package edu.whimc.journey.spigot.command.common;

import edu.whimc.journey.common.data.DataAccessException;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An abstract class describing a command node that must be invoked by an in-game player.
 */
public abstract class PlayerCommandNode extends CommandNode {

  /**
   * General constructor.
   *
   * @param parent       the parent command
   * @param permission   the permission
   * @param description  the description
   * @param primaryAlias primary alias of this command
   */
  public PlayerCommandNode(@Nullable CommandNode parent,
                           @Nullable Permission permission,
                           @NotNull String description,
                           @NotNull String primaryAlias) {
    super(parent, permission, description, primaryAlias);
  }

  @Override
  public final boolean onWrappedCommand(@NotNull CommandSender sender,
                                        @NotNull Command command,
                                        @NotNull String label,
                                        @NotNull String[] args,
                                        @NotNull Map<String, String> flags) throws DataAccessException {
    Player player;
    if (!(sender instanceof Player)) {
      sendCommandUsageError(sender, CommandError.ONLY_PLAYER);
      return false;
    }
    player = (Player) sender;
    return onWrappedPlayerCommand(player, command, label, args, flags);
  }

  /**
   * The same as {@link #onWrappedCommand(CommandSender, Command, String, String[], Map)},
   * but gives the player because this command must be executed by a player.
   *
   * @param player  the player
   * @param command the command
   * @param label   the label (root command)
   * @param args    the arguments after the root command
   * @param flags   the flags (arguments starting with "-")
   * @return true if successful
   * @throws DataAccessException a data access exception
   */
  public abstract boolean onWrappedPlayerCommand(@NotNull Player player,
                                                 @NotNull Command command,
                                                 @NotNull String label,
                                                 @NotNull String[] args,
                                                 @NotNull Map<String, String> flags)
      throws DataAccessException;
}
