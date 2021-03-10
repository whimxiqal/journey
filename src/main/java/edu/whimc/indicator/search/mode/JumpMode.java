package edu.whimc.indicator.search.mode;

import edu.whimc.indicator.api.search.Mode;
import edu.whimc.indicator.search.LocationLocatable;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

public class JumpMode implements Mode<LocationLocatable, World> {
  @Override
  public Set<LocationLocatable> getDestinations(LocationLocatable origin) {
    Set<LocationLocatable> locations = new HashSet<>();
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
        for (int offXFull = offX * offX; offXFull <= 1; offXFull++) {
          for (int offZFull = offZ * offZ; offZFull <= 1; offZFull++) {
            if (!origin.getBlockAtOffset(offXFull*offX, 1, offZFull*offZ).isPassable()) {
              break pathCheck;
            }
            if (!origin.getBlockAtOffset(offXFull*offX, 2, offZFull*offZ).isPassable()) {
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
