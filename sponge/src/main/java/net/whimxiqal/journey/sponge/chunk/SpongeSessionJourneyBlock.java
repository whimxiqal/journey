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

package net.whimxiqal.journey.sponge.chunk;

import java.util.Optional;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.proxy.JourneyBlock;
import net.whimxiqal.journey.proxy.JourneyDoor;
import net.whimxiqal.journey.search.flag.FlagSet;
import net.whimxiqal.journey.sponge.util.MaterialGroups_16_5;
import net.whimxiqal.journey.sponge.util.MaterialGroups_17;
import net.whimxiqal.journey.sponge.util.SpongeUtil;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.Direction;

/**
 * Sponge's representation of a Minecraft block
 *
 * @param cell       the location of this cell
 * @param state      the Sponge state for the block
 * @param stateBelow the Sponge state for the block directly below this one
 * @param flagSet    the set of flags used for the session for which this block is used
 */
public record SpongeSessionJourneyBlock(Cell cell,
                                        BlockState state,
                                        BlockState stateBelow,
                                        FlagSet flagSet) implements JourneyBlock {

  @Override
  public boolean isAir() {
    return state.type().equals(BlockTypes.AIR.get());
  }

  @Override
  public boolean isNetherPortal() {
    return state.type().equals(BlockTypes.NETHER_PORTAL.get());
  }

  @Override
  public boolean isWater() {
    return state.type().equals(BlockTypes.WATER.get());
  }

  @Override
  public boolean isPressurePlate() {
    return state.type().equals(BlockTypes.ACACIA_PRESSURE_PLATE.get())
        || state.type().equals(BlockTypes.BIRCH_PRESSURE_PLATE.get())
        || state.type().equals(BlockTypes.CRIMSON_PRESSURE_PLATE.get())
        || state.type().equals(BlockTypes.DARK_OAK_PRESSURE_PLATE.get())
        || state.type().equals(BlockTypes.HEAVY_WEIGHTED_PRESSURE_PLATE.get())
        || state.type().equals(BlockTypes.JUNGLE_PRESSURE_PLATE.get())
        || state.type().equals(BlockTypes.LIGHT_WEIGHTED_PRESSURE_PLATE.get())
        || state.type().equals(BlockTypes.OAK_PRESSURE_PLATE.get())
        || state.type().equals(BlockTypes.POLISHED_BLACKSTONE_PRESSURE_PLATE.get())
        || state.type().equals(BlockTypes.SPRUCE_PRESSURE_PLATE.get())
        || state.type().equals(BlockTypes.STONE_PRESSURE_PLATE.get())
        || state.type().equals(BlockTypes.WARPED_PRESSURE_PLATE.get());
  }

  @Override
  public boolean isClimbable() {
    return switch (Journey.get().proxy().assetVersion()) {
      case MINECRAFT_16_5 -> MaterialGroups_16_5.isClimbable(state.type());
      case MINECRAFT_17 -> MaterialGroups_17.isClimbable(state.type());
    };
  }

  @Override
  public boolean isPassable() {
    boolean belowTwoBlocksTall = switch (Journey.get().proxy().assetVersion()) {
      case MINECRAFT_16_5 -> MaterialGroups_16_5.isTwoBlocksTall(stateBelow.type());
      case MINECRAFT_17 -> MaterialGroups_17.isTwoBlocksTall(stateBelow.type());
    };
    return !belowTwoBlocksTall && SpongeUtil.isPassable(state);
  }

  @Override
  public boolean isLaterallyPassable() {
    boolean belowTwoBlocksTall = switch (Journey.get().proxy().assetVersion()) {
      case MINECRAFT_16_5 -> MaterialGroups_16_5.isTwoBlocksTall(stateBelow.type());
      case MINECRAFT_17 -> MaterialGroups_17.isTwoBlocksTall(stateBelow.type());
    };
    return !belowTwoBlocksTall && SpongeUtil.isLaterallyPassable(state, flagSet);
  }

  @Override
  public boolean isVerticallyPassable() {
    return SpongeUtil.isVerticallyPassable(state);
  }

  @Override
  public boolean canStandOn() {
    return SpongeUtil.canStandOn(state);
  }

  @Override
  public boolean canStandIn() {
    return SpongeUtil.canStandIn(state);
  }

  @Override
  public float hardness() {
    return 1;  // TODO
  }

  @Override
  public double height() {
    return switch (Journey.get().proxy().assetVersion()) {
      case MINECRAFT_16_5 -> MaterialGroups_16_5.height(state.type());
      case MINECRAFT_17 -> MaterialGroups_17.height(state.type());
    };
  }

  @Override
  public Optional<JourneyDoor> asDoor() {
    if (state.get(Keys.DOOR_HINGE).isEmpty()) {
      // not a door
      return Optional.empty();
    }
    Optional<Direction> direction = state.get(Keys.DIRECTION);
    Optional<Boolean> isOpen = state.get(Keys.IS_OPEN);
    if (direction.isEmpty() || isOpen.isEmpty()) {
      // invalid, we need a direction and isOpen value
      return Optional.empty();
    }
    boolean iron = state.type().equals(BlockTypes.IRON_DOOR.get());
    return Optional.of(new SpongeJourneyDoor(direction.get(), isOpen.get(), iron));
  }


}
