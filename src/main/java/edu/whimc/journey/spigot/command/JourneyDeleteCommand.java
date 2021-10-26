package edu.whimc.journey.spigot.command;

import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.journey.spigot.command.delete.JourneyDeletePublicCommand;
import edu.whimc.journey.spigot.command.delete.JourneyDeleteMyCommand;
import org.jetbrains.annotations.Nullable;

public class JourneyDeleteCommand extends FunctionlessCommandNode {
  public JourneyDeleteCommand(@Nullable CommandNode parent) {
    super(parent, null,
        "Delete a path destination",
        "delete");
    addChildren(new JourneyDeletePublicCommand(this));
    addChildren(new JourneyDeleteMyCommand(this));
  }
}
