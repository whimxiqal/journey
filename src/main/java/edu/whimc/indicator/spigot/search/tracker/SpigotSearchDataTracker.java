package edu.whimc.indicator.spigot.search.tracker;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.search.tracker.SearchDataTracker;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import org.bukkit.World;

public class SpigotSearchDataTracker extends SearchDataTracker<LocationCell, World> {

  public SpigotSearchDataTracker() {
    super(Indicator.getInstance().getDataManager());
  }

}
