package edu.whimc.indicator.search;

import edu.whimc.indicator.api.search.TwoLevelBreadthFirstSearch;
import edu.whimc.indicator.path.SpigotLocatable;
import edu.whimc.indicator.search.mode.FlyMode;
import edu.whimc.indicator.search.mode.JumpMode;
import edu.whimc.indicator.search.mode.WalkMode;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class IndicatorSearch extends TwoLevelBreadthFirstSearch<SpigotLocatable, World> {

  public IndicatorSearch(Player player) {
    registerMode(new WalkMode());
    registerMode(new JumpMode());
    if (player.getGameMode().equals(GameMode.CREATIVE)) {
      registerMode(new FlyMode());
    }
  }

}
