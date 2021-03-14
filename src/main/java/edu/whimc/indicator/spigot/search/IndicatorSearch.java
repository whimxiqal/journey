package edu.whimc.indicator.spigot.search;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.api.search.TwoLevelBreadthFirstSearch;
import edu.whimc.indicator.spigot.cache.DebugManager;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.search.mode.FlyMode;
import edu.whimc.indicator.spigot.search.mode.JumpMode;
import edu.whimc.indicator.spigot.search.mode.WalkMode;
import edu.whimc.indicator.spigot.util.Format;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class IndicatorSearch extends TwoLevelBreadthFirstSearch<LocationCell, World> {

  public IndicatorSearch(Player player) {
    // Modes
    registerMode(new WalkMode());
    registerMode(new JumpMode());
    if (player.getGameMode().equals(GameMode.CREATIVE)) {
      registerMode(new FlyMode());
    }

    // Links
    Indicator.getInstance().getNetherManager().makeLinks().forEach(this::registerLink);

    // Callbacks
    DebugManager debugManager = Indicator.getInstance().getDebugManager();
    setStartTrailSearchCallback((origin, destination) -> {
      debugManager.broadcastDebugMessage(Format.debug("Began a trail search: "));
      debugManager.broadcastDebugMessage(Format.debug(
          origin.toString()
              + " -> "));
      debugManager.broadcastDebugMessage(Format.debug(destination.toString()));
    });
    setFinishTrailSearchCallback((origin, destination) -> {
      debugManager.broadcastDebugMessage(Format.debug("Finished a trail search: "));
      debugManager.broadcastDebugMessage(Format.debug(
          origin.toString()
              + " -> "));
      debugManager.broadcastDebugMessage(Format.debug(destination.toString()));
    });
    setMemoryCapacityErrorCallback((origin, destination) -> {
      debugManager.broadcastDebugMessage(Format.debug("Began a trail search: "));
      debugManager.broadcastDebugMessage(Format.debug(
          origin.toString()
              + " -> "));
      debugManager.broadcastDebugMessage(Format.debug(destination.toString()));
    });
  }

}
