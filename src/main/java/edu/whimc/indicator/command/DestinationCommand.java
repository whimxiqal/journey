package edu.whimc.indicator.command;

import com.google.common.collect.Lists;
import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.command.common.CommandNode;
import edu.whimc.indicator.command.common.Parameter;
import edu.whimc.indicator.command.common.ParameterSuppliers;
import edu.whimc.indicator.destination.IndicatorDestination;
import edu.whimc.indicator.path.SpigotLocatable;
import edu.whimc.indicator.util.Format;
import edu.whimc.indicator.util.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DestinationCommand extends CommandNode {
  public DestinationCommand() {
    super(null,
        Permissions.DESTINATION_PERMISSION,
        "Set the destination of a player",
        "destination");
    addSubcommand(Parameter.builder()
        .supplier(ParameterSuppliers.ONLINE_PLAYER)
        .next(Parameter.builder()
            .supplier(Parameter.ParameterSupplier.builder()
                .strict(false)
                .allowedEntries(prev -> Lists.newArrayList("location"))
                .usage("location <x> <y> <z> <world>")
                .build())
            .build())
        .build(), "Set a specific location as the destination");
  }

  @Override
  public boolean onWrappedCommand(@NotNull CommandSender sender,
                                  @NotNull Command command,
                                  @NotNull String label,
                                  @NotNull String[] args) {
    if (args.length < 6) {
      sender.sendMessage(Format.error("Too few arguments!"));
      return false;
    }

    Player player = Bukkit.getServer().getPlayer(args[0]);
    if (player == null) {
      sender.sendMessage(Format.error("That player can not be found"));
      return false;
    }

    if (!args[1].equalsIgnoreCase("location")) {
      sender.sendMessage(Format.error("Unexpected parameter"));
      return false;
    }

    try {
      int x = Integer.parseInt(args[2]);
      int y = Integer.parseInt(args[3]);
      int z = Integer.parseInt(args[4]);
      World world = Bukkit.getWorld(args[5]);

      if (world == null) {
        sender.sendMessage(Format.error("That's not a valid world"));
        return false;
      }

      if (x < -100000 || x > 100000 || y < -100000 || y > 100000 || z < -100000 || z > 100000) {
        sender.sendMessage(Format.error("Your inputs are out of range"));
        return false;
      }

      IndicatorDestination destination = new IndicatorDestination(
          Indicator.getInstance(),
          new SpigotLocatable(x, y, z, world));

      Indicator.getInstance().getDestinationManager().put(player.getUniqueId(), destination);
      sender.sendMessage(Format.success("Set " + player.getName() + "'s destination to: "));
      sender.sendMessage(Format.success(x + ", " + y + ", " + z + " in " + world.getName()));
      return true;
    } catch (NumberFormatException e) {
      sender.sendMessage(Format.error("Your numbers could not be read"));
      return false;
    }
  }
}
