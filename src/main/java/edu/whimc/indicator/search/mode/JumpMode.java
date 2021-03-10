package edu.whimc.indicator.search.mode;

import edu.whimc.indicator.api.search.Mode;
import edu.whimc.indicator.path.SpigotLocatable;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

public class JumpMode implements Mode<SpigotLocatable, World> {
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
    if (!origin.getBlockAtOffset(0, 2, 0).isPassable()) {
      return locations;
    }

    // 1 block away
    for (int offX = -1; offX <= 1; offX++) {
      for (int offZ = -1; offZ <= 1; offZ++) {
        if (offX == 0 && offZ == 0) continue;
        pathCheck:
        for (int offXIn = offX * offX /* normalize sign */; offXIn >= 0; offXIn--) {
          for (int offZIn = offZ * offZ /* normalize sign */; offZIn >= 0; offZIn--) {
            if (!origin.getBlockAtOffset(offXIn*offX, 1, offZIn*offZ).isPassable()) {
              break pathCheck;
            }
            if (!origin.getBlockAtOffset(offXIn*offX, 2, offZIn*offZ).isPassable()) {
              break pathCheck;
            }
          }
        }
        if (!origin.getBlockAtOffset(offX, 0, offZ).isPassable()) {
          locations.add(origin.createLocatableAtOffset(offX, 1, offZ));
          break;
        }
      }
    }

    return locations;
  }
}
