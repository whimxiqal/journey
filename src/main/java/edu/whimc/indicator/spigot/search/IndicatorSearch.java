package edu.whimc.indicator.spigot.search;

import edu.whimc.indicator.api.search.TwoLevelBreadthFirstSearch;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.search.mode.FlyMode;
import edu.whimc.indicator.spigot.search.mode.JumpMode;
import edu.whimc.indicator.spigot.search.mode.WalkMode;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class IndicatorSearch extends TwoLevelBreadthFirstSearch<LocationCell, World> {

  public IndicatorSearch(Player player) {
    registerMode(new WalkMode());
    registerMode(new JumpMode());
    if (player.getGameMode().equals(GameMode.CREATIVE)) {
      registerMode(new FlyMode());
    }
  }

}
