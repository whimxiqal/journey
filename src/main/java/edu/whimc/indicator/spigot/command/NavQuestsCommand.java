package edu.whimc.indicator.spigot.command;

import edu.whimc.indicator.common.tools.BufferedFunction;
import edu.whimc.indicator.common.util.Extra;
import edu.whimc.indicator.spigot.command.common.CommandError;
import edu.whimc.indicator.spigot.command.common.CommandNode;
import edu.whimc.indicator.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.indicator.spigot.command.common.Parameter;
import edu.whimc.indicator.spigot.command.common.PlayerCommandNode;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.indicator.spigot.util.Permissions;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.blackvein.quests.Quest;
import me.blackvein.quests.Quests;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NavQuestsCommand extends FunctionlessCommandNode {

  public NavQuestsCommand(@Nullable CommandNode parent, @NotNull Quests quests) {
    super(parent, Permissions.TRAIL_USE_PERMISSION,
        "Blaze trails to destinations for quests using the Quests plugin",
        "quests");
    addChildren(new NavNextQuestCommand(this, quests));
  }

  public static class NavNextQuestCommand extends PlayerCommandNode {

    private final Quests quests;

    public NavNextQuestCommand(@Nullable CommandNode parent, @NotNull Quests quests) {
      super(parent, Permissions.TRAIL_USE_PERMISSION,
          "Blaze trails to your quest objectives",
          "next");
      this.quests = quests;
      BufferedFunction<Player, List<String>> questFunction = new BufferedFunction<>(player -> {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Quests");
        if (!(plugin instanceof Quests)) {
          return new LinkedList<>();
        }
        return quests.getQuester(player.getUniqueId())
            .getCurrentQuests()
            .keySet()
            .stream()
            .map(quest -> Extra.quoteStringWithSpaces(quest.getName()))
            .collect(Collectors.toList());
      }, 1000);
      addSubcommand(Parameter.builder()
          .supplier(Parameter.ParameterSupplier.builder()
              .usage("<quest>")
              .allowedEntries((src, list) -> {
                if (src instanceof Player) {
                  return questFunction.apply((Player) src);
                } else {
                  return new LinkedList<>();
                }
              })
              .strict(false)
              .build())
          .build(), "Blaze trails to your next destination for a quest");
    }

    @Override
    public boolean onWrappedPlayerCommand(@NotNull Player player,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args,
                                          @NotNull Map<String, String> flags) {

      if (args.length == 0) {
        sendCommandError(player, CommandError.FEW_ARGUMENTS);
        return false;
      }

      Quest quest = quests.getQuest(args[0]);
      if (quest == null) {
        player.spigot().sendMessage(Format.error("That quest doesn't exist"));
        return false;
      }

      if (!quests.getQuester(player.getUniqueId()).getCurrentQuests().containsKey(quest)) {
        player.spigot().sendMessage(Format.error("You are not doing that quest"));
        return false;
      }

      LinkedList<Location> locationsToReach = quests.getQuester(player.getUniqueId()).getCurrentStage(quest).getLocationsToReach();
      if (locationsToReach.isEmpty()) {
        player.spigot().sendMessage(Format.error("That quest has no destination"));
        return false;
      }

      LocationCell endLocation = new LocationCell(locationsToReach.getFirst());

      NavCommand.blazeTrailTo(player, endLocation, flags);
      return true;
    }
  }
}
