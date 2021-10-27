package edu.whimc.journey.spigot.command.save;

import edu.whimc.journey.common.data.DataAccessException;
import edu.whimc.journey.common.data.PublicEndpointManager;
import edu.whimc.journey.common.util.Validator;
import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.command.common.CommandError;
import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.Parameter;
import edu.whimc.journey.spigot.command.common.PlayerCommandNode;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.journey.spigot.util.Permissions;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A command to allow saving of new public search endpoints.
 */
public class JourneySavePublicCommand extends PlayerCommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent command
   */
  public JourneySavePublicCommand(@Nullable CommandNode parent) {
    super(parent,
        Permissions.JOURNEY_TO_PUBLIC_EDIT,
        "Save your current location as a public path destination",
        "public");

    addSubcommand(Parameter.builder()
        .supplier(Parameter.ParameterSupplier.builder()
            .usage("<name>")
            .build())
        .build(), "Save with this name");
  }

  @Override
  public boolean onWrappedPlayerCommand(@NotNull Player player,
                                        @NotNull Command command,
                                        @NotNull String label,
                                        @NotNull String[] args,
                                        @NotNull Map<String, String> flags) throws DataAccessException {

    if (args.length == 0) {
      sendCommandUsageError(player, CommandError.FEW_ARGUMENTS);
      return false;
    }

    String name = args[0];
    if (Validator.isInvalidDataName(name)) {
      player.spigot().sendMessage(Format.error("That name is invalid."));
      return false;
    }

    PublicEndpointManager<LocationCell, World> publicEndpointManager = JourneySpigot.getInstance()
        .getDataManager()
        .getPublicEndpointManager();

    String existingName = publicEndpointManager.getServerEndpointName(new LocationCell(player.getLocation()));
    if (existingName != null) {
      player.spigot().sendMessage(Format.error("Server location ",
          Format.toPlain(Format.note(existingName)),
          " already exists at that location!"));
      return false;
    }

    LocationCell existingCell = publicEndpointManager.getServerEndpoint(name);
    if (existingCell != null) {
      player.spigot().sendMessage(Format.error("A server location already exists with that name at",
          Format.toPlain(Format.locationCell(existingCell, Format.DEFAULT)),
          "!"));
      return false;
    }

    publicEndpointManager.addServerEndpoint(new LocationCell(player.getLocation()), name);
    player.spigot().sendMessage(Format.success("Added server location named ",
        Format.toPlain(Format.note(name))));
    return true;
  }
}