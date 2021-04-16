/*
 * Copyright 2021 Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package edu.whimc.indicator.spigot.command;

import com.google.common.collect.Lists;
import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.path.Endpoint;
import edu.whimc.indicator.spigot.command.common.CommandError;
import edu.whimc.indicator.spigot.command.common.CommandNode;
import edu.whimc.indicator.spigot.command.common.Parameter;
import edu.whimc.indicator.spigot.command.common.ParameterSuppliers;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.indicator.spigot.util.Permissions;
import me.blackvein.quests.Quest;
import me.blackvein.quests.Quests;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
                .allowedEntries(prev -> {
                  List<String> allowed = Lists.newArrayList("location", "here");
                  if (Bukkit.getPluginManager().isPluginEnabled("Quests")) {
                    allowed.add("quests");
                  }
                  return allowed;
                })
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
                                  @NotNull String[] args,
                                  @NotNull Set<String> flags) {
    if (args.length < 2) {
      sendCommandError(sender, CommandError.FEW_ARGUMENTS);
      return false;
    }

    Player player = Bukkit.getServer().getPlayer(args[0]);
    if (player == null) {
      sendCommandError(sender, CommandError.NO_PLAYER);
      return false;
    }

    LocationCell endLocation;
    Endpoint<JavaPlugin, LocationCell, World> endpoint;
    World world;
    int x;
    int y;
    int z;
    switch (args[1].toLowerCase()) {
      case "location":
        if (args.length < 6) {
          sendCommandError(sender, CommandError.FEW_ARGUMENTS);
          return false;
        }

        try {
          world = Bukkit.getWorld(args[2]);
          x = Integer.parseInt(args[3]);
          y = Integer.parseInt(args[4]);
          z = Integer.parseInt(args[5]);

          if (world == null) {
            sender.sendMessage(Format.error("That's not a valid world"));
            return false;
          }

          if (x < -100000 || x > 100000 || y < -100000 || y > 100000 || z < -100000 || z > 100000) {
            sender.sendMessage(Format.error("Your inputs are out of range"));
            return false;
          }

          endLocation = new LocationCell(x, y, z, world);
          endpoint = new Endpoint<>(
              Indicator.getInstance(),
              endLocation,
              loc -> loc.distanceToSquared(endLocation) < 9);  // Finish within 3 blocks

          break;

        } catch (NumberFormatException e) {
          sender.sendMessage(Format.error("Your numbers could not be read"));
          return false;
        }

      case "here":

        world = player.getLocation().getWorld();
        x = player.getLocation().getBlockX();
        y = player.getLocation().getBlockY();
        z = player.getLocation().getBlockZ();

        if (world == null) {
          sender.sendMessage(Format.error("Your world could not be found."));
          return false;
        }

        endLocation = new LocationCell(x, y, z, world);
        endpoint = new Endpoint<>(
            Indicator.getInstance(),
            endLocation,
            loc -> loc.distanceToSquared(endLocation) < 9);  // Finish within 3 blocks

        break;

      case "quest":

        Plugin plugin = Bukkit.getPluginManager().getPlugin("Quests");
        if (plugin == null) {
          sendCommandError(sender, "Quests is not loaded");
          return false;
        }

        if (!(plugin instanceof Quests)) {
          sendCommandError(sender, "An internal error occurred");
          Indicator.getInstance().getLogger().warning("Plugin Quests could be found but could not be cast to Quests");
          return false;
        }
        Quests quests = (Quests) plugin;

        Quest quest = quests.getQuest(args[2]);
        if (quest == null) {
          sendCommandError(sender, "That quest doesn't exist");
          return false;
        }

        if (!quests.getQuester(player.getUniqueId()).getCurrentQuests().containsKey(quest)) {
          sendCommandError(sender, "That player is not doing that quest");
          return false;
        }

        LinkedList<Location> locationsToReach = quests.getQuester(player.getUniqueId()).getCurrentStage(quest).getLocationsToReach();
        if (locationsToReach.isEmpty()) {
          sendCommandError(sender, "That quest has no destination");
          return false;
        }

        endLocation = new LocationCell(locationsToReach.getFirst());
        endpoint = new Endpoint<>(
            Indicator.getInstance(),
            endLocation,
            loc -> loc.distanceToSquared(endLocation) < 9);  // Finish within 3 blocks

        break;

      default:
        sender.sendMessage(Format.error("Unexpected parameter."));
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
