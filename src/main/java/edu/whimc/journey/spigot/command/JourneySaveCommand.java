package edu.whimc.journey.spigot.command;

import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.journey.spigot.command.save.JourneySaveMyCommand;
import edu.whimc.journey.spigot.command.save.JourneySavePublicCommand;
import org.jetbrains.annotations.Nullable;

/**
 * A command to allow saving of new search endpoints.
 */
public class JourneySaveCommand extends FunctionlessCommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent command
   */
  public JourneySaveCommand(@Nullable CommandNode parent) {
    super(parent, null,
        "Save a new path destination",
        "save");
    addChildren(new JourneySavePublicCommand(this));
    addChildren(new JourneySaveMyCommand(this));
  }
}
