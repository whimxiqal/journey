package edu.whimc.indicator.spigot.command;

import com.google.common.collect.Lists;
import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.api.path.Endpoint;
import edu.whimc.indicator.spigot.command.common.CommandNode;
import edu.whimc.indicator.spigot.command.common.Parameter;
import edu.whimc.indicator.spigot.command.common.ParameterSuppliers;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.indicator.spigot.util.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class EndpointCommand extends CommandNode {
  public EndpointCommand() {
    super(null,
        Permissions.DESTINATION_PERMISSION,
        "Set the endpoint of a player's trail",
        "endpoint");
    addSubcommand(Parameter.builder()
        .supplier(ParameterSuppliers.ONLINE_PLAYER)
        .next(Parameter.builder()
            .supplier(Parameter.ParameterSupplier.builder()
                .strict(false)
                .allowedEntries(prev -> Lists.newArrayList("location", "here"))
                .usage("location")
                .build())
            .next(Parameter.builder()
                .supplier(ParameterSuppliers.WORLD)
                .next(Parameter.builder()
                    .supplier(Parameter.ParameterSupplier.builder()
                        .usage("<x> <y> <z>")
                        .strict(false)
                        .build())
                    .build())
                .build())
            .build())
        .build(), "Set a specific location as the destination");
  }

  @Override
  public boolean onWrappedCommand(@NotNull CommandSender sender,
                                  @NotNull Command command,
                                  @NotNull String label,
                                  @NotNull String[] args) {
    if (args.length < 2) {
      sender.sendMessage(Format.error("Too few arguments!"));
      return false;
    }

    Player player = Bukkit.getServer().getPlayer(args[0]);
    if (player == null) {
      sender.sendMessage(Format.error("That player can not be found"));
      return false;
    }

    Endpoint<JavaPlugin, LocationCell, World> endpoint;
    switch (args[1].toLowerCase()) {
      case "location":
        if (args.length < 6) {
          sender.sendMessage(Format.error("Too few arguments!"));
          return false;
        }

        try {
          World world = Bukkit.getWorld(args[2]);
          int x = Integer.parseInt(args[3]);
          int y = Integer.parseInt(args[4]);
          int z = Integer.parseInt(args[5]);

          if (world == null) {
            sender.sendMessage(Format.error("That's not a valid world"));
            return false;
          }

          if (x < -100000 || x > 100000 || y < -100000 || y > 100000 || z < -100000 || z > 100000) {
            sender.sendMessage(Format.error("Your inputs are out of range"));
            return false;
          }

          endpoint = new Endpoint<>(
              Indicator.getInstance(),
              new LocationCell(x, y, z, world));
          break;

        } catch (NumberFormatException e) {
          sender.sendMessage(Format.error("Your numbers could not be read"));
          return false;
        }

      case "here":

        World world = player.getLocation().getWorld();
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();

        if (world == null) {
          player.sendMessage(Format.error("Your world could not be found."));
          return false;
        }

        endpoint = new Endpoint<>(
            Indicator.getInstance(),
            new LocationCell(x, y, z, world));
        break;

      default:
        player.sendMessage(Format.error("Unexpected parameter."));
        return false;

    }

    Indicator.getInstance().getEndpointManager().put(player.getUniqueId(), endpoint);
    sender.sendMessage(Format.success("Set " + player.getName() + "'s endpoint to: "));
    sender.sendMessage(Format.success("  ["
        + Format.INFO + endpoint.getLocation().getX()
        + Format.SUCCESS + ", "
        + Format.INFO + endpoint.getLocation().getY()
        + Format.SUCCESS + ", "
        + Format.INFO + endpoint.getLocation().getZ()
        + Format.SUCCESS + " in "
        + Format.INFO + endpoint.getLocation().getDomain().getName()
        + Format.SUCCESS + "]"));
    return true;
  }
}
