package edu.whimc.journey.spigot.command;

import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.journey.spigot.command.list.JourneyListPublicCommand;
import edu.whimc.journey.spigot.command.list.JourneyListMineCommand;
import org.jetbrains.annotations.Nullable;

public class JourneyListCommand extends FunctionlessCommandNode {
  public JourneyListCommand(@Nullable CommandNode parent) {
    super(parent, null,
        "List path destinations",
        "list");
    addChildren(new JourneyListPublicCommand(this));
    addChildren(new JourneyListMineCommand(this));
  }
}
