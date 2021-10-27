package edu.whimc.journey.spigot.command;

import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.journey.spigot.command.delete.JourneyDeleteMyCommand;
import edu.whimc.journey.spigot.command.delete.JourneyDeletePublicCommand;
import org.jetbrains.annotations.Nullable;

/**
 * A command to host various subcommands that allow deletions of search endpoints.
 */
public class JourneyDeleteCommand extends FunctionlessCommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent command
   */
  public JourneyDeleteCommand(@Nullable CommandNode parent) {
    super(parent, null,
        "Delete a path destination",
        "delete");
    addChildren(new JourneyDeletePublicCommand(this));
    addChildren(new JourneyDeleteMyCommand(this));
  }
}
