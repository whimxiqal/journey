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

package edu.whimc.indicator.spigot.search.mode;

import edu.whimc.indicator.api.path.Mode;
import edu.whimc.indicator.api.path.ModeType;
import edu.whimc.indicator.spigot.path.LocationCell;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WalkMode implements Mode<LocationCell, World> {

  private static final Set<Material> IMPASSABLE_MATERIALS = new HashSet<>();

  public WalkMode() {
    IMPASSABLE_MATERIALS.add(Material.LAVA);
    IMPASSABLE_MATERIALS.add(Material.NETHER_PORTAL);
    IMPASSABLE_MATERIALS.add(Material.END_PORTAL);
  }

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
              Block offsetBlock = origin.getBlockAtOffset(offXIn * offX /* get sign back */, offY, offZIn * offZ);
              if (!offsetBlock.isPassable()) {
                continue outerZ;  // Barrier - invalid move
              }
              if (IMPASSABLE_MATERIALS.contains(offsetBlock.getType())) {
                continue outerZ;  // Impassable - invalid move
              }
            }
          }
        }
        for (int offY = -1; offY >= -4; offY--) {  // Check for floor anywhere up to a 3 block fall
          if (!origin.getBlockAtOffset(offX, offY, offZ).isPassable()) {
            if (origin.getBlockAtOffset(offX, offY + 2, offZ).getType().equals(Material.WATER)) {
              break;  // Water (drowning) - invalid destination
            }
            LocationCell other = origin.createLocatableAtOffset(offX, offY+1, offZ);
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
