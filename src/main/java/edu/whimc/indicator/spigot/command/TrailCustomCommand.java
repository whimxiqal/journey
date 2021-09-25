package edu.whimc.indicator.spigot.command;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.data.CustomEndpointManager;
import edu.whimc.indicator.common.data.DataAccessException;
import edu.whimc.indicator.common.tools.BufferedFunction;
import edu.whimc.indicator.common.util.Extra;
import edu.whimc.indicator.common.util.Validator;
import edu.whimc.indicator.spigot.command.common.CommandError;
import edu.whimc.indicator.spigot.command.common.CommandNode;
import edu.whimc.indicator.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.indicator.spigot.command.common.Parameter;
import edu.whimc.indicator.spigot.command.common.ParameterSuppliers;
import edu.whimc.indicator.spigot.command.common.PlayerCommandNode;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.indicator.spigot.util.Permissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TrailCustomCommand extends FunctionlessCommandNode {

  public TrailCustomCommand(@NotNull CommandNode parent) {
    super(parent,
        Permissions.TRAIL_USE_PERMISSION,
        "Use custom locations in paths",
        "custom");

    addChildren(new TrailCustomBlazeCommand(this));
    addChildren(new TrailCustomDeleteCommand(this));
    addChildren(new TrailCustomListCommand(this));
    addChildren(new TrailCustomSaveCommand(this));
  }

  private static BufferedFunction<Player, List<String>> bufferedCustomLocationsFunction() {
    return new BufferedFunction<>(player -> {
      try {
        return Indicator.getInstance().getDataManager()
            .getCustomEndpointManager()
            .getCustomEndpoints(player.getUniqueId()).keySet()
            .stream().map(Extra::quoteStringWithSpaces).collect(Collectors.toList());
      } catch (DataAccessException e) {
        return new LinkedList<>();
      }
    }, 1000);
  }

  public static class TrailCustomBlazeCommand extends PlayerCommandNode {

    public TrailCustomBlazeCommand(@NotNull CommandNode parent) {
      super(parent,
          Permissions.TRAIL_USE_PERMISSION,
          "Blaze a trail to a custom destination",
          "blaze");

      BufferedFunction<Player, List<String>> customLocationsFunction = bufferedCustomLocationsFunction();
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

      addSubcommand(Parameter.builder()
          .supplier(ParameterSuppliers.WORLD)
          .next(Parameter.builder()
              .supplier(Parameter.ParameterSupplier.builder()
                  .usage("<x> <y> <z> [name]")
                  .strict(false)
                  .build())
              .build())
          .build(), "Make a new trail to a custom location");

    }

    @Override
    public boolean onWrappedPlayerCommand(@NotNull Player player,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args,
                                          @NotNull Map<String, String> flags) throws DataAccessException {

      LocationCell endLocation;
      World world;
      int x;
      int y;
      int z;

      if (args.length == 0) {
        sendCommandError(player, CommandError.FEW_ARGUMENTS);
        return false;
      }

      CustomEndpointManager<LocationCell, World> customEndpointManager = Indicator.getInstance()
          .getDataManager()
          .getCustomEndpointManager();
      try {
        if (args.length >= 4) {
          world = Bukkit.getWorld(args[0]);
          x = Extra.isCommandOffset(args[1])
              ? Extra.calcCommandOffset(player.getLocation().getBlockX(), args[1])
              : Integer.parseInt(args[1]);
          y = Extra.isCommandOffset(args[2])
              ? Extra.calcCommandOffset(player.getLocation().getBlockY(), args[2])
              : Integer.parseInt(args[2]);
          z = Extra.isCommandOffset(args[3])
              ? Extra.calcCommandOffset(player.getLocation().getBlockZ(), args[3])
              : Integer.parseInt(args[3]);

          if (world == null) {
            player.spigot().sendMessage(Format.error("That's not a valid world"));
            return false;
          }

          if (x < -100000 || x > 100000 || y < -100000 || y > 100000 || z < -100000 || z > 100000) {
            player.spigot().sendMessage(Format.error("Your inputs are out of range"));
            return false;
          }

          endLocation = new LocationCell(x, y, z, world);
        } else {
          endLocation = customEndpointManager.getCustomEndpoint(player.getUniqueId(), args[0]);

          if (endLocation == null) {
            player.spigot().sendMessage(Format.error("The custom location ", Format.toPlain(Format.note(args[0])), " could not be found"));
            return false;
          }
        }
      } catch (IllegalArgumentException e) {
        player.spigot().sendMessage(Format.error("Your numbers could not be read"));
        return false;
      }

      if (TrailCommand.blazeTrailTo(player, endLocation, flags)) {

        // Check if we should save a custom endpoint
        if (args.length >= 5) {
          if (customEndpointManager.hasCustomEndpoint(player.getUniqueId(), endLocation)) {
            player.spigot().sendMessage(Format.error("A custom location already exists at that location!"));
            return false;
          }
          if (customEndpointManager.hasCustomEndpoint(player.getUniqueId(), args[4])) {
            player.spigot().sendMessage(Format.error("A custom location already exists with that name!"));
            return false;
          }
          if (!Validator.isValidDataName(args[5])) {
            player.spigot().sendMessage(Format.error("Your custom name ", Format.toPlain(Format.note(args[4])), " contains illegal characters"));
            return false;
          }
          // Save it!
          customEndpointManager.addCustomEndpoint(player.getUniqueId(), endLocation, args[4]);
          player.spigot().sendMessage(Format.success("Saved your custom location with name ", Format.toPlain(Format.note(args[4])), "!"));
        }

        return true;
      } else {
        return false;
      }

    }
  }

  public static class TrailCustomDeleteCommand extends PlayerCommandNode {

    public TrailCustomDeleteCommand(@Nullable CommandNode parent) {
      super(parent,
          Permissions.TRAIL_USE_PERMISSION,
          "Delete a saved custom destination",
          "delete");
      BufferedFunction<Player, List<String>> customLocationsFunction = bufferedCustomLocationsFunction();
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
        sendCommandError(player, CommandError.FEW_ARGUMENTS);
        return false;
      }

      CustomEndpointManager<LocationCell, World> endpointManager = Indicator.getInstance()
          .getDataManager()
          .getCustomEndpointManager();
      if (endpointManager.hasCustomEndpoint(player.getUniqueId(), args[0])) {
        Indicator.getInstance().getDataManager().getCustomEndpointManager().removeCustomEndpoint(player.getUniqueId(), args[0]);
        player.spigot().sendMessage(Format.success("The custom location ", Format.toPlain(Format.note(args[0])), " has been removed"));
        return true;
      } else {
        player.spigot().sendMessage(Format.error("The custom location ", Format.toPlain(Format.note(args[0])), " could not be found"));
        return false;
      }
    }

  }

  public static class TrailCustomListCommand extends PlayerCommandNode {

    public TrailCustomListCommand(@Nullable CommandNode parent) {
      super(parent,
          Permissions.TRAIL_USE_PERMISSION,
          "List saved custom destinations",
          "list");
      addSubcommand(Parameter.builder()
              .supplier(Parameter.ParameterSupplier.builder()
                  .strict(false)
                  .usage("[page]")
                  .build())
              .build(),
          "View saved custom locations");
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
          .getCustomEndpointManager()
          .getCustomEndpoints(player.getUniqueId());

      if (cells.isEmpty()) {
        player.spigot().sendMessage(Format.warn("You have no saved custom locations yet!"));
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

      player.spigot().sendMessage(Format.success("Custom Locations - Page ",
          Format.toPlain(Format.note(Integer.toString(pageNumber))),
          " of ",
          Format.toPlain(Format.note(Integer.toString(chatPage.getTotalPages())))));
      Arrays.stream(chatPage.getLines()).forEach(player::sendMessage);

      return true;
    }

  }

  public static class TrailCustomSaveCommand extends PlayerCommandNode {

    public TrailCustomSaveCommand(@Nullable CommandNode parent) {
      super(parent,
          Permissions.TRAIL_USE_PERMISSION,
          "Save your current location as a custom trail location",
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

      CustomEndpointManager<LocationCell, World> customEndpointManager = Indicator.getInstance()
          .getDataManager()
          .getCustomEndpointManager();

      String existingName = customEndpointManager.getCustomEndpointName(player.getUniqueId(), new LocationCell(player.getLocation()));
      if (existingName != null) {
        player.spigot().sendMessage(Format.error("Custom location ", Format.toPlain(Format.note(existingName)), " already exists at that location!"));
        return false;
      }

      LocationCell existingCell = customEndpointManager.getCustomEndpoint(player.getUniqueId(), name);
      if (existingCell != null) {
        player.spigot().sendMessage(Format.error("A custom location already exists with that name at",
            Format.toPlain(Format.locationCell(existingCell, Format.DEFAULT)),
            "!"));
        return false;
      }

      customEndpointManager.addCustomEndpoint(player.getUniqueId(), new LocationCell(player.getLocation()), name);
      player.spigot().sendMessage(Format.success("Added custom location named ", Format.toPlain(Format.note(name))));
      return true;
    }
  }

}
