/*
 * MIT License
 *
 * Copyright (c) whimxiqal
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

package net.whimxiqal.journey.bukkit.chunk;

import java.util.Optional;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import net.whimxiqal.journey.bukkit.util.MaterialGroups;
import net.whimxiqal.journey.proxy.JourneyBlock;
import net.whimxiqal.journey.proxy.JourneyDoor;
import net.whimxiqal.journey.search.flag.FlagSet;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;

public record BukkitSessionJourneyBlock(Cell cell,
                                        BlockData data,
                                        FlagSet flagSet) implements JourneyBlock {

  @Override
  public boolean isNetherPortal() {
    return data.getMaterial() == Material.NETHER_PORTAL;
  }

  @Override
  public boolean isWater() {
    return data.getMaterial() == Material.WATER;
  }

  @Override
  public boolean isPressurePlate() {
    return MaterialGroups.PRESSURE_PLATES.contains(data.getMaterial());
  }

  @Override
  public boolean isClimbable() {
    return MaterialGroups.isClimbable(data.getMaterial());
  }

  @Override
  public boolean isPassable() {
    return BukkitUtil.isPassable(data);
  }

  @Override
  public boolean isLaterallyPassable() {
    return BukkitUtil.isLaterallyPassable(data, flagSet);
  }

  @Override
  public boolean isVerticallyPassable() {
    return BukkitUtil.isVerticallyPassable(data);
  }

  @Override
  public boolean canStandOn() {
    return BukkitUtil.canStandOn(data);
  }

  @Override
  public boolean canStandIn() {
    return BukkitUtil.canStandIn(data);
  }

  @Override
  public float hardness() {
    return data.getMaterial().getHardness();
  }

  @Override
  public double height() {
    return MaterialGroups.height(data.getMaterial());
  }

  @Override
  public Optional<JourneyDoor> asDoor() {
    if (data instanceof Door) {
      return Optional.of(new BukkitJourneyDoor((Door) data));
    }
    return Optional.empty();
  }


}
