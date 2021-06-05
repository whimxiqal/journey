package edu.whimc.indicator.spigot.command;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.data.DataManager;
import edu.whimc.indicator.common.util.Extra;
import edu.whimc.indicator.common.util.Validator;
import edu.whimc.indicator.spigot.command.common.*;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.indicator.spigot.util.Permissions;
import edu.whimc.indicator.spigot.util.UuidToWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TrailCustomCommand extends FunctionlessCommandNode {

  public TrailCustomCommand(@NotNull CommandNode parent) {
    super(parent,
        Permissions.TRAIL_PERMISSION,
        "Use custom locations in trails",
        "custom");

    addChildren(new TrailCustomBlazeCommand(this));
    addChildren(new TrailCustomDeleteCommand(this));
    addChildren(new TrailCustomListCommand(this));
    addChildren(new TrailCustomSaveCommand(this));
  }

  public static class TrailCustomBlazeCommand extends PlayerCommandNode {

    public TrailCustomBlazeCommand(@NotNull CommandNode parent) {
      super(parent,
          Permissions.TRAIL_PERMISSION,
          "Blaze a trail to a custom destination",
          "blaze");
      addSubcommand(Parameter.builder()
          .supplier(Parameter.ParameterSupplier.builder()
              .usage("<name>")
              .build())
          .build(), "Use a previously saved custom location");
      addSubcommand(Parameter.builder()
          .supplier(ParameterSuppliers.WORLD)
          .next(Parameter.builder()
              .supplier(Parameter.ParameterSupplier.builder()
                  .usage("<x> <y> <z> [timeout] [name]")
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
                                          @NotNull Set<String> flags) {

      LocationCell endLocation;
      World world;
      int x;
      int y;
      int z;

      if (args.length == 0) {
        sendCommandError(player, CommandError.FEW_ARGUMENTS);
        return false;
      }

      DataManager<LocationCell, World, UuidToWorld> dataManager = Indicator.getInstance().getDataManager();
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
            player.sendMessage(Format.error("That's not a valid world"));
            return false;
          }

          if (x < -100000 || x > 100000 || y < -100000 || y > 100000 || z < -100000 || z > 100000) {
            player.sendMessage(Format.error("Your inputs are out of range"));
            return false;
          }

          endLocation = new LocationCell(x, y, z, world);
        } else {
          endLocation = dataManager.getCustomEndpointManager().getCustomEndpoint(player.getUniqueId(), args[0]);

          if (endLocation == null) {
            player.sendMessage(Format.error("The custom location ", Format.INFO + "" + args[0], " could not be found"));
            return false;
          }
        }
      } catch (IllegalArgumentException e) {
        player.sendMessage(Format.error("Your numbers could not be read"));
        return false;
      }

      if (TrailCommand.blazeTrailTo(player, endLocation, flags, TrailCommand.getTimeout(player, args, 4))) {

        // Check if we should save a custom endpoint
        if (!dataManager.getCustomEndpointManager().hasCustomEndpoint(player.getUniqueId(), endLocation)) {
          // Save it!
          if (args.length >= 6) {
            if (Validator.isValidDataName(args[5])) {
              dataManager.getCustomEndpointManager().addCustomEndpoint(player.getUniqueId(), endLocation, args[5]);
            } else {
              player.sendMessage(Format.error("Your custom name ", Format.note(args[5]), " contains illegal characters"));
              return false;
            }
          } else {
            dataManager.getCustomEndpointManager().addCustomEndpoint(player.getUniqueId(), endLocation);
          }
        }

        player.sendMessage(Format.success("Blazing a trail to "));
        player.sendMessage(Format.success(" " + Format.locationCell(endLocation, Format.SUCCESS) + " !"));
        return true;
      } else {
        return false;
      }

    }
  }

  public static class TrailCustomSaveCommand extends PlayerCommandNode {

    public TrailCustomSaveCommand(@Nullable CommandNode parent) {
      super(parent,
          Permissions.TRAIL_PERMISSION,
          "Save your current location as a custom trail location",
          "save");
      addSubcommand(Parameter.builder()
          .supplier(Parameter.ParameterSupplier.builder()
              .usage("[name]")
              .build())
          .build(), "Save with this name");
    }

    @Override
    public boolean onWrappedPlayerCommand(@NotNull Player player,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args,
                                          @NotNull Set<String> flags) {
      DataManager.CustomEndpoint<LocationCell, World, UuidToWorld> customEndpointManager = Indicator.getInstance()
          .getDataManager()
          .getCustomEndpointManager();

      if (customEndpointManager.hasCustomEndpoint(player.getUniqueId(), new LocationCell(player.getLocation()))) {
        player.sendMessage(Format.error("A custom location already exists at that location!"));
        return false;
      }

      if (args.length > 0) {
        String name = args[0];
        if (!Validator.isValidDataName(name)) {
          player.sendMessage(Format.error("That name is invalid"));
          return false;
        }

        if (customEndpointManager.hasCustomEndpoint(player.getUniqueId(), name)) {
          player.sendMessage(Format.error("A custom location already exists with that name!"));
          return false;
        }

        customEndpointManager.addCustomEndpoint(player.getUniqueId(), new LocationCell(player.getLocation()), name);
        player.sendMessage(Format.success("Added custom location named ", Format.note(name)));
      } else {
        Indicator.getInstance().getDataManager()
            .getCustomEndpointManager()
            .addCustomEndpoint(player.getUniqueId(), new LocationCell(player.getLocation()));
        player.sendMessage(Format.success("Added custom location"));
      }
      return true;
    }
  }

  public static class TrailCustomListCommand extends PlayerCommandNode {

    public TrailCustomListCommand(@Nullable CommandNode parent) {
      super(parent,
          Permissions.TRAIL_PERMISSION,
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
                                          @NotNull Set<String> flags) {
      int pageNumber;
      if (args.length > 0) {
        try {
          pageNumber = Integer.parseInt(args[0]);

          if (pageNumber < 0) {
            player.sendMessage(Format.error("The page number may not be negative!"));
            return false;
          }
        } catch (NumberFormatException e) {
          player.sendMessage(Format.error("The page number must be an integer"));
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
        player.sendMessage(Format.warn("You have no saved custom locations yet!"));
        return true;
      }

      List<Map.Entry<String, LocationCell>> sortedEntryList = new ArrayList<>(cells.entrySet());
      sortedEntryList.sort(Map.Entry.comparingByKey());

      StringBuilder builder = new StringBuilder();
      sortedEntryList.forEach(entry -> builder.append(
          Format.note(Format.ACCENT2 + entry.getKey(),
              " > ",
              Format.locationCell(entry.getValue(), Format.DEFAULT),
              "\n")));
      ChatPaginator.ChatPage chatPage = ChatPaginator.paginate(builder.toString(),
          pageNumber,
          ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH,
          ChatPaginator.CLOSED_CHAT_PAGE_HEIGHT - 1);

      pageNumber = Math.min(pageNumber, chatPage.getTotalPages());

      player.sendMessage(Format.success("Custom Locations - Page ",
          Format.note(pageNumber),
          " of ",
          Format.note(chatPage.getTotalPages())));
      Arrays.stream(chatPage.getLines()).forEach(player::sendMessage);

      return true;
    }

  }

  public static class TrailCustomDeleteCommand extends PlayerCommandNode {

    public TrailCustomDeleteCommand(@Nullable CommandNode parent) {
      super(parent,
          Permissions.TRAIL_PERMISSION,
          "Delete a saved custom destination",
          "delete");
      addSubcommand(Parameter.builder()
          .supplier(Parameter.ParameterSupplier.builder()
              .usage("<name>")
              .build())
          .build(), "Remove a previously saved custom location");
    }

    @Override
    public boolean onWrappedPlayerCommand(@NotNull Player player,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args,
                                          @NotNull Set<String> flags) {
      if (args.length < 1) {
        sendCommandError(player, CommandError.FEW_ARGUMENTS);
        return false;
      }

      DataManager.CustomEndpoint<LocationCell, World, UuidToWorld> endpointManager = Indicator.getInstance()
          .getDataManager()
          .getCustomEndpointManager();
      if (endpointManager.hasCustomEndpoint(player.getUniqueId(), args[0])) {
        Indicator.getInstance().getDataManager().getCustomEndpointManager().removeCustomEndpoint(player.getUniqueId(), args[0].toLowerCase());
        player.sendMessage(Format.success("The custom location ", Format.INFO + "" + args[0], " has been removed"));
        return true;
      } else {
        player.sendMessage(Format.error("The custom location ", Format.INFO + "" + args[0], " could not be found"));
        return false;
      }
    }

  }

}
