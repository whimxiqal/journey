package edu.whimc.indicator.spigot.command;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.data.DataAccessException;
import edu.whimc.indicator.common.data.ServerEndpointManager;
import edu.whimc.indicator.common.tools.BufferedSupplier;
import edu.whimc.indicator.common.util.Extra;
import edu.whimc.indicator.common.util.Validator;
import edu.whimc.indicator.spigot.command.common.*;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.indicator.spigot.util.Permissions;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class TrailServerCommand extends FunctionlessCommandNode {

  public TrailServerCommand(@NotNull CommandNode parent) {
    super(parent,
        Permissions.TRAIL_USE_PERMISSION,
        "Use server-wide locations in trails",
        "server");

    addChildren(new TrailServerBlazeCommand(this));
    addChildren(new TrailServerDeleteCommand(this));
    addChildren(new TrailServerListCommand(this));
    addChildren(new TrailServerSaveCommand(this));
  }

  private static BufferedSupplier<List<String>> bufferedServerLocationSupplier() {
    return new BufferedSupplier<>(() -> {
      try {
        return Indicator.getInstance().getDataManager()
            .getServerEndpointManager()
            .getServerEndpoints().keySet()
            .stream().map(Extra::quoteStringWithSpaces).collect(Collectors.toList());
      } catch (DataAccessException e) {
        return new LinkedList<>();
      }
    }, 1000);
  }

  public static class TrailServerBlazeCommand extends PlayerCommandNode {

    public TrailServerBlazeCommand(@NotNull CommandNode parent) {
      super(parent,
          Permissions.TRAIL_USE_PERMISSION,
          "Blaze a trail to a server destination",
          "blaze");

      BufferedSupplier<List<String>> serverLocationSupplier = bufferedServerLocationSupplier();
      addSubcommand(Parameter.builder()
          .supplier(Parameter.ParameterSupplier.builder()
              .usage("<name>")
              .allowedEntries((src, prev) -> serverLocationSupplier.get())
              .strict(false)
              .build())
          .build(), "Use a name");

    }

    @Override
    public boolean onWrappedPlayerCommand(@NotNull Player player,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args,
                                          @NotNull Map<String, String> flags) throws DataAccessException {

      if (args.length == 0) {
        sendCommandError(player, CommandError.FEW_ARGUMENTS);
        return false;
      }

      LocationCell endLocation;
      ServerEndpointManager<LocationCell, World> serverEndpointManager = Indicator.getInstance()
          .getDataManager()
          .getServerEndpointManager();
      try {
        endLocation = serverEndpointManager.getServerEndpoint(args[0]);

        if (endLocation == null) {
          player.spigot().sendMessage(Format.error("The server location ", Format.toPlain(Format.note(args[0])), " could not be found"));
          return false;
        }
      } catch (IllegalArgumentException e) {
        player.spigot().sendMessage(Format.error("Your numbers could not be read"));
        return false;
      }

      if (TrailCommand.blazeTrailTo(player, endLocation, flags)) {

        // Check if we should save a server endpoint
        if (args.length >= 5) {
          if (serverEndpointManager.hasServerEndpoint(endLocation)) {
            player.spigot().sendMessage(Format.error("A server location already exists at that location!"));
            return false;
          }
          if (serverEndpointManager.hasServerEndpoint(args[4])) {
            player.spigot().sendMessage(Format.error("A server location already exists with that name!"));
            return false;
          }
          if (!Validator.isValidDataName(args[4])) {
            player.spigot().sendMessage(Format.error("Your server name ", Format.toPlain(Format.note(args[4])), " contains illegal characters"));
            return false;
          }
          // Save it!
          serverEndpointManager.addServerEndpoint(endLocation, args[4]);
          player.spigot().sendMessage(Format.success("Saved your server location with name ",
              Format.toPlain(Format.note(args[4])),
              "!"));
        }

        return true;
      } else {
        return false;
      }

    }
  }

  public static class TrailServerDeleteCommand extends PlayerCommandNode {

    public TrailServerDeleteCommand(@Nullable CommandNode parent) {
      super(parent,
          Permissions.TRAIL_MANAGE_PERMISSION,
          "Delete a saved server destination",
          "delete");

      BufferedSupplier<List<String>> serverLocationSupplier = bufferedServerLocationSupplier();
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
        sendCommandError(player, CommandError.FEW_ARGUMENTS);
        return false;
      }

      ServerEndpointManager<LocationCell, World> endpointManager = Indicator.getInstance()
          .getDataManager()
          .getServerEndpointManager();
      if (endpointManager.hasServerEndpoint(args[0])) {
        Indicator.getInstance().getDataManager().getServerEndpointManager().removeServerEndpoint(args[0]);
        player.spigot().sendMessage(Format.success("The server location ", Format.toPlain(Format.note(args[0])), " has been removed"));
        return true;
      } else {
        player.spigot().sendMessage(Format.error("The server location ", Format.toPlain(Format.note(args[0])), " could not be found"));
        return false;
      }
    }


  }

  public static class TrailServerListCommand extends PlayerCommandNode {


    public TrailServerListCommand(@Nullable CommandNode parent) {
      super(parent,
          Permissions.TRAIL_USE_PERMISSION,
          "List saved server destinations",
          "list");
      addSubcommand(Parameter.builder()
              .supplier(Parameter.ParameterSupplier.builder()
                  .strict(false)
                  .usage("[page]")
                  .build())
              .build(),
          "View saved server locations");
    }

    @Override
    public boolean onWrappedPlayerCommand(@NotNull Player player,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args,
                                          @NotNull Map<String, String> flags) throws DataAccessException {
      int pageNumber;
      if (args.length > 0) {
        try {
          pageNumber = Integer.parseInt(args[0]);

          if (pageNumber < 0) {
            player.spigot().sendMessage(Format.error("The page number may not be negative!"));
            return false;
          }
        } catch (NumberFormatException e) {
          player.spigot().sendMessage(Format.error("The page number must be an integer"));
          return false;
        }
      } else {
        pageNumber = 1;
      }

      Map<String, LocationCell> cells = Indicator.getInstance()
          .getDataManager()
          .getServerEndpointManager()
          .getServerEndpoints();

      if (cells.isEmpty()) {
        player.spigot().sendMessage(Format.warn("There are no saved server locations yet!"));
        return true;
      }

      List<Map.Entry<String, LocationCell>> sortedEntryList = new ArrayList<>(cells.entrySet());
      sortedEntryList.sort(Map.Entry.comparingByKey());

      StringBuilder builder = new StringBuilder();
      sortedEntryList.forEach(entry -> builder
          .append(Format.ACCENT2)
          .append(entry.getKey())
          .append(Format.DEFAULT)
          .append(" > ")
          .append(Format.toPlain(Format.locationCell(entry.getValue(), Format.DEFAULT)))
          .append("\n"));
      ChatPaginator.ChatPage chatPage = ChatPaginator.paginate(builder.toString(),
          pageNumber,
          ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH,
          ChatPaginator.CLOSED_CHAT_PAGE_HEIGHT - 1);

      pageNumber = Math.min(pageNumber, chatPage.getTotalPages());

      player.spigot().sendMessage(Format.success("Server Locations - Page ",
          Format.toPlain(Format.note(Integer.toString(pageNumber))),
          " of ",
          Format.toPlain(Format.note(Integer.toString(chatPage.getTotalPages())))));
      Arrays.stream(chatPage.getLines()).forEach(player::sendMessage);

      return true;
    }

  }

  public static class TrailServerSaveCommand extends PlayerCommandNode {

    public TrailServerSaveCommand(@Nullable CommandNode parent) {
      super(parent,
          Permissions.TRAIL_MANAGE_PERMISSION,
          "Save your current location as a server trail location",
          "save");
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
        sendCommandError(player, CommandError.FEW_ARGUMENTS);
        return false;
      }

      String name = args[0];
      if (!Validator.isValidDataName(name)) {
        player.spigot().sendMessage(Format.error("That name is invalid"));
        return false;
      }

      ServerEndpointManager<LocationCell, World> serverEndpointManager = Indicator.getInstance()
          .getDataManager()
          .getServerEndpointManager();

      String existingName = serverEndpointManager.getServerEndpointName(new LocationCell(player.getLocation()));
      if (existingName != null) {
        player.spigot().sendMessage(Format.error("Server location ", Format.toPlain(Format.note(existingName)), " already exists at that location!"));
        return false;
      }

      LocationCell existingCell = serverEndpointManager.getServerEndpoint(name);
      if (existingCell != null) {
        player.spigot().sendMessage(Format.error("A server location already exists with that name at",
            Format.toPlain(Format.locationCell(existingCell, Format.DEFAULT)),
            "!"));
        return false;
      }

      serverEndpointManager.addServerEndpoint(new LocationCell(player.getLocation()), name);
      player.spigot().sendMessage(Format.success("Added server location named ", Format.toPlain(Format.note(name))));
      return true;
    }
  }

}
