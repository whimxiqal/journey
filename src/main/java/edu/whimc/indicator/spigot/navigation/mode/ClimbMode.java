package edu.whimc.indicator.spigot.navigation.mode;

import edu.whimc.indicator.common.navigation.ModeType;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;

public final class ClimbMode extends SpigotMode {

  private static final Set<Material> climbable = Set.of(Material.LADDER, Material.VINE);

  public ClimbMode(Set<Material> forcePassable) {
    super(forcePassable);
  }

  @Override
  protected void collectDestinations(LocationCell origin) {

    // TODO we have to make sure that the ladders and vines are oriented correctly
    //  and that the vines have a solid block behind it
    tryToClimbAdjacent(origin.getBlockAtOffset(1, 0, 0));
    tryToClimbAdjacent(origin.getBlockAtOffset(-1, 0, 0));
    tryToClimbAdjacent(origin.getBlockAtOffset(0, 0, 1));
    tryToClimbAdjacent(origin.getBlockAtOffset(0, 0, -1));
    tryToClimbAdjacent(origin.getBlockAtOffset(0, -1, 0));

    // Going up is a different story
    if (climbable.contains(origin.getBlock().getType())) {
      if (isVerticallyPassable(origin.getBlockAtOffset(0, 1, 0))
          && isVerticallyPassable(origin.getBlockAtOffset(0, 2, 0))) {
        accept(origin.createLocatableAtOffset(0, 1, 0), 1.0d);
      } else {
        reject(origin.createLocatableAtOffset(0, 1, 0));
      }
    }

  }

  private void tryToClimbAdjacent(Block block) {
    if (climbable.contains(block.getType())) {
      accept(new LocationCell(block.getLocation()), 1.0d);
    } else {
      reject(new LocationCell(block.getLocation()));
    }
  }

  @Override
  public ModeType getType() {
    return ModeType.CLIMB;
  }
}
