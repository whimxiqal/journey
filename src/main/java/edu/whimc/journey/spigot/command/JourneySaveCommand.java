package edu.whimc.journey.spigot.command;

import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.journey.spigot.command.save.JourneySavePublicCommand;
import edu.whimc.journey.spigot.command.save.JourneySaveMyCommand;
import org.jetbrains.annotations.Nullable;

public class JourneySaveCommand extends FunctionlessCommandNode {
  public JourneySaveCommand(@Nullable CommandNode parent) {
    super(parent, null,
        "Save a new path destination",
        "save");
    addChildren(new JourneySavePublicCommand(this));
    addChildren(new JourneySaveMyCommand(this));
  }
}
