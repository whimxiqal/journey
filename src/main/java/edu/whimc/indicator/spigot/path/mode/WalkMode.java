/*
 * Copyright 2021 Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package edu.whimc.indicator.spigot.path.mode;

import edu.whimc.indicator.common.path.Mode;
import edu.whimc.indicator.common.path.ModeType;
import edu.whimc.indicator.common.path.ModeTypes;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.util.SpigotUtil;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class WalkMode implements Mode<LocationCell, World> {

  @Override
  public Map<LocationCell, Double> getDestinations(LocationCell origin) {
    Map<LocationCell, Double> locations = new HashMap<>();

    // Can you drop into an inhabitable block?
    if (SpigotUtil.canStandOn(origin.getBlockAtOffset(0, -2, 0))
        || SpigotUtil.canStandIn(origin.getBlockAtOffset(0, -1, 0))) {
      locations.put(origin.createLocatableAtOffset(0, -1, 0), 1d);
    }

    // Can we even stand here?
    if (!SpigotUtil.canStandOn(origin.getBlockAtOffset(0, -1, 0))
        && !SpigotUtil.canStandIn(origin.getBlockAtOffset(0, 0, 0))) {
      return locations;
    }

    // 1 block away
    for (int offX = -1; offX <= 1; offX++) {
      outerZ:
      for (int offZ = -1; offZ <= 1; offZ++) {
        // For the diagonal points, check that the path is clear in both
        //  lateral directions as well as diagonally
        for (int offXIn = offX * offX /* normalize sign */; offXIn >= 0; offXIn--) {
          for (int offZIn = offZ * offZ /* normalize sign */; offZIn >= 0; offZIn--) {
            if (offXIn == 0 && offZIn == 0) continue;
            for (int offY = 0; offY <= 1; offY++) { // Check two blocks tall
              if (!SpigotUtil.isLaterallyPassable(origin.getBlockAtOffset(offXIn * offX /* get sign back */, offY, offZIn * offZ))) {
                continue outerZ;  // Barrier - invalid move
              }
            }
          }
        }
        // We can move to offX and offY laterally
        if (!SpigotUtil.isVerticallyPassable(origin.getBlockAtOffset(offX, 0, offZ))) {
          // We can just stand right here (carpets, slabs, etc.)
          LocationCell other = origin.createLocatableAtOffset(offX, 0, offZ);
          locations.put(other, origin.distanceTo(other));
        }
        for (int offY = -1; offY >= -4; offY--) {  // Check for floor anywhere up to a 3 block fall
          if (!SpigotUtil.isVerticallyPassable(origin.getBlockAtOffset(offX, offY, offZ))) {
            if (origin.getBlockAtOffset(offX, offY + 2, offZ).getType().equals(Material.WATER)) {
              break;  // Water (drowning) - invalid destination
            }
            LocationCell other = origin.createLocatableAtOffset(offX, offY + 1, offZ);
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
    return ModeTypes.WALK;
  }
}
