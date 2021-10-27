package edu.whimc.journey.spigot.command;

import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.journey.spigot.command.list.JourneyListMineCommand;
import edu.whimc.journey.spigot.command.list.JourneyListPublicCommand;
import org.jetbrains.annotations.Nullable;

/**
 * A command to hold subcommands that list search endpoints.
 */
public class JourneyListCommand extends FunctionlessCommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent command
   */
  public JourneyListCommand(@Nullable CommandNode parent) {
    super(parent, null,
        "List path destinations",
        "list");
    addChildren(new JourneyListPublicCommand(this));
    addChildren(new JourneyListMineCommand(this));
  }
}
