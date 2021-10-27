package edu.whimc.journey.spigot.command;

import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.journey.spigot.command.to.JourneyToMyCommand;
import edu.whimc.journey.spigot.command.to.JourneyToPublicCommand;
import edu.whimc.journey.spigot.command.to.JourneyToQuestCommand;
import edu.whimc.journey.spigot.command.to.JourneyToSurfaceCommand;
import me.blackvein.quests.Quests;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * A command to allow the calculation of a path to some destination endpoint.
 *
 * @see edu.whimc.journey.common.search.SearchSession
 * @see edu.whimc.journey.spigot.search.PlayerSearchSession
 */
public class JourneyToCommand extends FunctionlessCommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent command
   */
  public JourneyToCommand(@Nullable CommandNode parent) {
    super(parent, null,
        "Commands for navigating to certain locations",
        "to");
    addChildren(new JourneyToPublicCommand(this));
    addChildren(new JourneyToMyCommand(this));
    addChildren(new JourneyToSurfaceCommand(this));

    // Quests plugin
    Plugin questsPlugin = Bukkit.getPluginManager().getPlugin("Quests");
    if (questsPlugin instanceof Quests) {
      addChildren(new JourneyToQuestCommand(this, (Quests) questsPlugin));
    }
  }

}
