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
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class FlyMode implements Mode<LocationCell, World> {
  @Override
  public Map<LocationCell, Double> getDestinations(LocationCell origin) {
    Map<LocationCell, Double> locations = new HashMap<>();

    // Check every block in a 3x3 grid centered around the current location
    for (int offX = -1; offX <= 1; offX++) {
      for (int offY = -1; offY <= 1; offY++) {
        outerZ:
        // Label so we can continue from these main loops when all checks fail
        for (int offZ = -1; offZ <= 1; offZ++) {
          // Checks for the block -- checks between the offset block and the original block,
          //  which would be 1 for just 1 offset variable, 4 for 2 offset variables,
          //  and 8 for 3 offset variables.
          for (int offXIn = offX * offX /* normalize sign */; offXIn >= 0; offXIn--) {
            for (int offYIn = offY * offY /* normalize sign */; offYIn >= 0; offYIn--) {
              for (int offZIn = offZ * offZ /* normalize sign */; offZIn >= 0; offZIn--) {
                // This is the origin, we don't want to move here
                if (offXIn == 0 && offYIn == 0 && offZIn == 0) continue;
                // Make sure we get the pillar of y values for the player's body
                for (int h = 0; h <= offYIn + 1; h++) {
                  if (!SpigotUtil.isPassable(origin.getBlockAtOffset(
                      offXIn * offX /* get sign back */,
                      offYIn * offY + h,
                      offZIn * offZ))) {
                    continue outerZ;
                  }
                }
              }
            }
          }
          LocationCell other = origin.createLocatableAtOffset(offX, offY, offZ);
          locations.put(other, origin.distanceTo(other));
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
