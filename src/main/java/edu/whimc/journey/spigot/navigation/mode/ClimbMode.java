package edu.whimc.journey.spigot.navigation.mode;

import edu.whimc.journey.common.navigation.ModeType;
import edu.whimc.journey.common.search.SearchSession;
import edu.whimc.journey.spigot.navigation.LocationCell;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * A mode to provide the results to whether a player can climb blocks around them,
 * like ladders or vines.
 */
public final class ClimbMode extends SpigotMode {

  private static final Set<Material> climbable = Set.of(Material.LADDER, Material.VINE);

  /**
   * General constructor.
   *
   * @param forcePassable a set of materials deemed passable
   */
  public ClimbMode(SearchSession<LocationCell, World> session, Set<Material> forcePassable) {
    super(session, forcePassable);
  }

  @Override
  protected void collectDestinations(LocationCell origin, @NotNull List<Option> options) {

    // TODO we have to make sure that the ladders and vines are oriented correctly
    //  and that the vines have a solid block behind it
    tryToClimbAdjacent(origin.getBlockAtOffset(1, 0, 0), options);
    tryToClimbAdjacent(origin.getBlockAtOffset(-1, 0, 0), options);
    tryToClimbAdjacent(origin.getBlockAtOffset(0, 0, 1), options);
    tryToClimbAdjacent(origin.getBlockAtOffset(0, 0, -1), options);
    tryToClimbAdjacent(origin.getBlockAtOffset(0, -1, 0), options);

    // Going up is a different story
    if (climbable.contains(origin.getBlock().getType())) {
      if (isVerticallyPassable(origin.getBlockAtOffset(0, 1, 0))
          && isVerticallyPassable(origin.getBlockAtOffset(0, 2, 0))) {
        accept(origin.createLocatableAtOffset(0, 1, 0), 1.0d, options);
      } else {
        reject(origin.createLocatableAtOffset(0, 1, 0));
      }
    }

  }

  private void tryToClimbAdjacent(Block block, List<Option> options) {
    if (climbable.contains(block.getType())) {
      accept(new LocationCell(block.getLocation()), 1.0d, options);
    } else {
      reject(new LocationCell(block.getLocation()));
    }
  }

  @Override
  public @NotNull ModeType getType() {
    return ModeType.CLIMB;
  }
}
