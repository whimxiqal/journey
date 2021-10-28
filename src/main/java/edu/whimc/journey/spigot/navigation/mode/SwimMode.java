package edu.whimc.journey.spigot.navigation.mode;

import edu.whimc.journey.common.navigation.ModeType;
import edu.whimc.journey.common.search.SearchSession;
import edu.whimc.journey.spigot.navigation.LocationCell;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Determines whether a humanoid entity can swim to various locations.
 */
public class SwimMode extends SpigotMode {

  /**
   * Default constructor.
   *
   * @param session       the search session
   * @param forcePassable the set of passable materials
   */
  public SwimMode(SearchSession<LocationCell, World> session, Set<Material> forcePassable) {
    super(session, forcePassable);
  }

  @Override
  public void collectDestinations(@NotNull LocationCell origin, @NotNull List<Option> options) {
    // TODO implement
  }

  @Override
  public @NotNull ModeType getType() {
    return ModeType.SWIM;
  }
}
