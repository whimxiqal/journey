package edu.whimc.journey.spigot.command.to;

import edu.whimc.journey.common.data.DataAccessException;
import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.PlayerCommandNode;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.journey.spigot.util.Permissions;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JourneyToSurfaceCommand extends PlayerCommandNode {

  public JourneyToSurfaceCommand(@Nullable CommandNode parent) {
    super(parent, Permissions.JOURNEY_TO_SURFACE_USE,
        "Journey to the surface, if you are in the overworld",
        "surface");
  }

  @Override
  public boolean onWrappedPlayerCommand(@NotNull Player player,
                                        @NotNull Command command,
                                        @NotNull String label,
                                        @NotNull String[] args,
                                        @NotNull Map<String, String> flags) throws DataAccessException {
    player.spigot().sendMessage(Format.error("This is not implemented yet!"));
    return false;
  }
}