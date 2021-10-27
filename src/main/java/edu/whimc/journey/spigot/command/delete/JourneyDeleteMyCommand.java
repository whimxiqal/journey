package edu.whimc.journey.spigot.command.delete;

import edu.whimc.journey.common.data.DataAccessException;
import edu.whimc.journey.common.data.PersonalEndpointManager;
import edu.whimc.journey.common.tools.BufferedFunction;
import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.command.JourneyCommand;
import edu.whimc.journey.spigot.command.common.CommandError;
import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.Parameter;
import edu.whimc.journey.spigot.command.common.PlayerCommandNode;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.journey.spigot.util.Permissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A command to delete a personal search endpoint.
 */
public class JourneyDeleteMyCommand extends PlayerCommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent command
   */
  public JourneyDeleteMyCommand(@Nullable CommandNode parent) {
    super(parent, Permissions.JOURNEY_TO_CUSTOM_USE,
        "Delete a saved personal location",
        "my");

    BufferedFunction<Player, List<String>> customLocationsFunction
        = JourneyCommand.bufferedPersonalEndpointFunction();
    addSubcommand(Parameter.builder()
        .supplier(Parameter.ParameterSupplier.builder()
            .usage("<name>")
            .allowedEntries((src, prev) -> {
              if (src instanceof Player) {
                return customLocationsFunction.apply((Player) src);
              } else {
                return new ArrayList<>();
              }
            }).build())
        .build(), "Use a previously saved custom location");
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

    PersonalEndpointManager<LocationCell, World> endpointManager = JourneySpigot.getInstance()
        .getDataManager()
        .getPersonalEndpointManager();
    if (endpointManager.hasCustomEndpoint(player.getUniqueId(), args[0])) {
      JourneySpigot.getInstance().getDataManager()
          .getPersonalEndpointManager()
          .removeCustomEndpoint(player.getUniqueId(), args[0]);
      player.spigot().sendMessage(Format.success("The custom location ",
          Format.toPlain(Format.note(args[0])), " has been removed."));
      return true;
    } else {
      player.spigot().sendMessage(Format.error("The custom location ",
          Format.toPlain(Format.note(args[0])), " could not be found."));
      return false;
    }
  }
}
