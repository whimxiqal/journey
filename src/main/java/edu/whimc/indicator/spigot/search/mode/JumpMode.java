package edu.whimc.indicator.spigot.search.mode;

import edu.whimc.indicator.api.path.Mode;
import edu.whimc.indicator.api.path.ModeType;
import edu.whimc.indicator.spigot.path.LocationCell;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class JumpMode implements Mode<LocationCell, World> {
  @Override
  public Map<LocationCell, Double> getDestinations(LocationCell origin) {
    Map<LocationCell, Double> locations = new HashMap<>();
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
      outerZ:
      for (int offZ = -1; offZ <= 1; offZ++) {
        for (int offXIn = offX * offX /* normalize sign */; offXIn >= 0; offXIn--) {
          for (int offZIn = offZ * offZ /* normalize sign */; offZIn >= 0; offZIn--) {
            if (offX == 0 && offZ == 0) continue;
            for (int offY = 1; offY <= 2; offY++) { // Check two blocks tall
              if (!origin.getBlockAtOffset(offXIn * offX /* get sign back */, offY, offZIn * offZ).isPassable()) {
                continue outerZ;
              }
            }
          }
        }
        if (!origin.getBlockAtOffset(offX, 0, offZ).isPassable()) {
          LocationCell other = origin.createLocatableAtOffset(offX, 1, offZ);
          locations.put(other, origin.distanceTo(other));
          break;
        }
      }
    }

    return locations;
  }

  @Override
  public ModeType getType() {
    return ModeTypes.JUMP;
  }
}
