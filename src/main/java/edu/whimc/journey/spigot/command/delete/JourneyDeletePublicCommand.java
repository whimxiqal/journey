package edu.whimc.journey.spigot.command.delete;

import edu.whimc.journey.common.data.DataAccessException;
import edu.whimc.journey.common.data.PublicEndpointManager;
import edu.whimc.journey.common.tools.BufferedSupplier;
import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.command.JourneyCommand;
import edu.whimc.journey.spigot.command.common.CommandError;
import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.Parameter;
import edu.whimc.journey.spigot.command.common.PlayerCommandNode;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.journey.spigot.util.Permissions;
import java.util.List;
import java.util.Map;
import org.bukkit.World;
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
    super(parent, Permissions.JOURNEY_TO_PUBLIC_EDIT,
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

    PublicEndpointManager<LocationCell, World> endpointManager = JourneySpigot.getInstance()
        .getDataManager()
        .getPublicEndpointManager();
    if (endpointManager.hasServerEndpoint(args[0])) {
      JourneySpigot.getInstance().getDataManager().getPublicEndpointManager().removeServerEndpoint(args[0]);
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
