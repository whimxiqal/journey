package edu.whimc.indicator.search.mode;

import edu.whimc.indicator.api.search.Mode;
import edu.whimc.indicator.path.SpigotLocatable;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

public class WalkMode implements Mode<SpigotLocatable, World> {
  @Override
  public Set<SpigotLocatable> getDestinations(SpigotLocatable origin) {
    Set<SpigotLocatable> locations = new HashSet<>();
    if (origin.getBlockAtOffset(0, -1, 0).isPassable()) {
      return locations;
    }
    if (!origin.getBlockAtOffset(0, 0, 0).isPassable()) {
      return locations;
    }
    if (!origin.getBlockAtOffset(0, 1, 0).isPassable()) {
      return locations;
    }

    // 1 block away
    for (int offX = -1; offX <= 1; offX++) {
      outerZ:
      for (int offZ = -1; offZ <= 1; offZ++) {
        // For the diagonal points, check that the path is clear in both
        //  lateral directions as well as diagonally
        for (int offXIn = Math.abs(offX) /* normalize sign */; offXIn >= 0; offXIn--) {
          for (int offZIn = Math.abs(offX) /* normalize sign */; offZIn >= 0; offZIn--) {
            if (offXIn == 0 && offZIn == 0) continue;
            for (int offY = 0; offY <= 1; offY++) { // Check two blocks tall
              if (!origin.getBlockAtOffset(offXIn * offX /* get sign back */, 1, offZIn * offZ).isPassable()) {
                continue outerZ;
              }
            }
          }
        }
        for (int offY = -1; offY >= -4; offY--) {  // Check for floor anywhere up to a 3 block fall
          if (!origin.getBlockAtOffset(offX, offY, offZ).isPassable()) {
            locations.add(origin.createLocatableAtOffset(offX, offY+1, offZ));
            break;
          }
        }
      }
    }
    return locations;
  }
}
