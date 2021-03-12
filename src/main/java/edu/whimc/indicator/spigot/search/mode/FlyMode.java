package edu.whimc.indicator.spigot.search.mode;

import edu.whimc.indicator.api.path.Mode;
import edu.whimc.indicator.api.path.ModeType;
import edu.whimc.indicator.spigot.path.LocationCell;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class FlyMode implements Mode<LocationCell, World> {
  @Override
  public Map<LocationCell, Double> getDestinations(LocationCell origin) {
    Map<LocationCell, Double> locations = new HashMap<>();
    if (!origin.getBlockAtOffset(0, 0, 0).isPassable()) {
      return locations;
    }
    if (!origin.getBlockAtOffset(0, 1, 0).isPassable()) {
      return locations;
    }

    // 1 block away
    for (int offX = -1; offX <= 1; offX++) {
      for (int offY = -1; offY <= 1; offY++) {
        outerZ:
        for (int offZ = -1; offZ <= 1; offZ++) {
          for (int offXIn = offX * offX /* normalize sign */; offXIn >= 0; offXIn--) {
            for (int offYIn = offY * offY /* normalize sign */; offYIn >= 0; offYIn--) {
              for (int offZIn = offZ * offZ /* normalize sign */; offZIn >= 0; offZIn--) {
                if (offX == 0 && offY == 0 && offZ == 0) continue;
                for (int h = 0; h <= offYIn + 1; h++) {
                  if (!origin.getBlockAtOffset(offXIn * offX /* get sign back */, offYIn * offY + h, offZIn * offZ).isPassable()) {
                    continue outerZ;
                  }
                }
              }
            }
            LocationCell other = origin.createLocatableAtOffset(offX, offY, offZ);
            locations.put(other, origin.distanceTo(other));
            break;
          }
        }
      }
    }

    return locations;
  }

  @Override
  public ModeType getType() {
    return ModeTypes.FLY;
  }
}
