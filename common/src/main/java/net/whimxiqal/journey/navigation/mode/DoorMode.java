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

package net.whimxiqal.journey.navigation.mode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.chunk.BlockProvider;
import net.whimxiqal.journey.chunk.Direction;
import net.whimxiqal.journey.navigation.Mode;
import net.whimxiqal.journey.navigation.ModeType;
import net.whimxiqal.journey.proxy.JourneyDoor;
import net.whimxiqal.journey.search.SearchSession;
import org.jetbrains.annotations.NotNull;

/**
 * The movement mode to handle if players can move through doors.
 *
 * @see SearchSession
 */
public final class DoorMode extends Mode {

  @Override
  public Collection<Option> getDestinations(Cell origin, BlockProvider blockProvider) throws ExecutionException, InterruptedException {
    // TODO check if there are buttons or levers nearby that may open the door
    List<Option> options = new LinkedList<>();
    Cell cell;
    boolean standingOnPressurePlate = blockProvider.toBlock(origin).isPressurePlate();

    // Pos X - East
    cell = origin.atOffset(1, 0, 0);
    Optional<JourneyDoor> door = blockProvider.toBlock(cell).asDoor();
    // Check if we found a door
    if (door.isPresent()) {
      // Check it's a solid floor
      if (!blockProvider.toBlock(origin.atOffset(1, -1, 0)).isVerticallyPassable()) {
        if (door.get().isIron()) {
          // Need to check if the door is blocking
          if (door.get().direction() == Direction.POSITIVE_Z
              || door.get().direction() == Direction.NEGATIVE_Z
              || door.get().isOpen()) {
            // Nothing blocking
            options.add(new Option(origin.atOffset(1, 0, 0)));
          } else {
            // We need to be able to open the door
            if (standingOnPressurePlate) {
              // We can step on a pressure plate to open it
              options.add(new Option(origin.atOffset(1, 0, 0)));
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          options.add(new Option(origin.atOffset(1, 0, 0)));
        }
      }
    }

    // Pos Z - North
    cell = origin.atOffset(0, 0, 1);
    door = blockProvider.toBlock(cell).asDoor();
    // Check if we found a door
    if (door.isPresent()) {
      // Check it's a solid floor
      if (!blockProvider.toBlock(origin.atOffset(1, -1, 0)).isVerticallyPassable()) {
        if (door.get().isIron()) {
          // Need to check if the door is blocking
          if (door.get().direction() == Direction.POSITIVE_X
              || door.get().direction() == Direction.NEGATIVE_X
              || door.get().isOpen()) {
            // Nothing blocking
            options.add(new Option(origin.atOffset(0, 0, 1)));
          } else {
            // We need to be able to open the door
            if (standingOnPressurePlate) {
              // We can step on a pressure plate to open it
              options.add(new Option(origin.atOffset(0, 0, 1)));
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          options.add(new Option(origin.atOffset(0, 0, 1)));
        }
      }
    }

    // Neg X - West
    cell = origin.atOffset(-1, 0, 0);
    door = blockProvider.toBlock(cell).asDoor();
    // Check if we found a door
    if (door.isPresent()) {
      // Check it's a solid floor
      if (!blockProvider.toBlock(origin.atOffset(1, -1, 0)).isVerticallyPassable()) {
        if (door.get().isIron()) {
          // Need to check if the door is blocking
          if (door.get().direction() == Direction.POSITIVE_Z
              || door.get().direction() == Direction.NEGATIVE_Z
              || door.get().isOpen()) {
            // Nothing blocking
            options.add(new Option(origin.atOffset(-1, 0, 0)));
          } else {
            // We need to be able to open the door
            if (standingOnPressurePlate) {
              // We can step on a pressure plate to open it
              options.add(new Option(origin.atOffset(-1, 0, 0)));
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          options.add(new Option(origin.atOffset(-1, 0, 0)));
        }
      }
    }

    // Neg Z - South
    cell = origin.atOffset(0, 0, -1);
    door = blockProvider.toBlock(cell).asDoor();
    // Check if we found a door
    if (door.isPresent()) {
      // Check it's a solid floor
      if (!blockProvider.toBlock(origin.atOffset(1, -1, 0)).isVerticallyPassable()) {
        if (door.get().isIron()) {
          // Need to check if the door is blocking
          if (door.get().direction() == Direction.POSITIVE_X
              || door.get().direction() == Direction.NEGATIVE_X
              || door.get().isOpen()) {
            // Nothing blocking
            options.add(new Option(origin.atOffset(0, 0, -1)));
          } else {
            // We need to be able to open the door
            if (standingOnPressurePlate) {
              // We can step on a pressure plate to open it
              options.add(new Option(origin.atOffset(0, 0, -1)));
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          options.add(new Option(origin.atOffset(0, 0, -1)));
        }
      }
    }

    return options;
  }

  @Override
  public @NotNull ModeType type() {
    return ModeType.DOOR;
  }
}
