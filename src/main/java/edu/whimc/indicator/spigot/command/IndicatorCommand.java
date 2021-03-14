package edu.whimc.indicator.spigot.command;

import edu.whimc.indicator.spigot.command.common.FunctionlessCommandNode;
import edu.whimc.indicator.spigot.util.Permissions;

public class IndicatorCommand extends FunctionlessCommandNode {

  public IndicatorCommand() {
    super(null, Permissions.INDICATOR_PERMISSION,
        "The root for all indicator commands",
        "indicator");
    addChildren(new IndicatorDebugCommand(this));
  }

}
