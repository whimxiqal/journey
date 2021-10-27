package edu.whimc.journey.spigot.command;

import edu.whimc.journey.spigot.command.admin.JourneyAdminDebugCommand;
import edu.whimc.journey.spigot.command.admin.JourneyAdminInvalidateCommand;
import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.journey.spigot.util.Permissions;
import org.jetbrains.annotations.Nullable;

/**
 * A command to provide admin commands.
 */
public class JourneyAdminCommand extends FunctionlessCommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent command
   */
  public JourneyAdminCommand(@Nullable CommandNode parent) {
    super(parent, Permissions.ADMIN,
        "All administrative commands",
        "admin");
    addChildren(new JourneyAdminDebugCommand(this));
    addChildren(new JourneyAdminInvalidateCommand(this));
  }

}
