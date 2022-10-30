/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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

package me.pietelite.journey.spigot.navigation.mode;

import java.util.List;
import java.util.Set;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.ModeType;
import me.pietelite.journey.common.search.SearchSession;
import me.pietelite.journey.spigot.util.MaterialGroups;
import me.pietelite.journey.spigot.util.SpigotUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.jetbrains.annotations.NotNull;

/**
 * The movement mode to handle if players can move through doors.
 *
 * @see SearchSession
 */
public final class DoorMode extends SpigotMode {

  /**
   * Default constructor.
   *
   * @param forcePassable set of materials that we can always pass through
   */
  public DoorMode(SearchSession session, Set<Material> forcePassable) {
    super(session, forcePassable);
  }

  @Override
  public void collectDestinations(@NotNull Cell origin, @NotNull List<Option> options) {
    // TODO check if there are buttons or levers nearby that may open the door

    Cell cell;
    BlockData block;
    // Pos X - East
    cell = origin.atOffset(1, 0, 0);
    block = SpigotUtil.getBlock(cell);
    // Check if we found a door
    if (block instanceof Door) {
      // Check it's a solid floor
      if (!isVerticallyPassable(SpigotUtil.getBlock(origin.atOffset(1, -1, 0)))) {
        Door doorBlock = (Door) block;
        if (block.getMaterial().equals(Material.IRON_DOOR)) {
          // Need to check if the door is blocking
          if (doorBlock.getFacing().equals(BlockFace.NORTH)
              || doorBlock.getFacing().equals(BlockFace.SOUTH)
              || doorBlock.isOpen()) {
            // Nothing blocking
            accept(origin.atOffset(1, 0, 0), 1.0d, options);
          } else {
            // We need to be able to open the door
            if (MaterialGroups.PRESSURE_PLATES.contains(SpigotUtil.getBlock(origin).getMaterial())) {
              // We can step on a pressure plate to open it
              accept(origin.atOffset(1, 0, 0), 1.0d, options);
            } else {
              reject(origin.atOffset(1, 0, 0));
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          accept(origin.atOffset(1, 0, 0), 1.0d, options);
        }
      } else {
        reject(origin.atOffset(1, -1, 0));
      }
    } else {
      reject(cell);
    }

    // Pos Z - North
    cell = origin.atOffset(0, 0, 1);
    block = SpigotUtil.getBlock(cell);
    // Check if we found a door
    if (block instanceof Door) {
      // Check it's a solid floor
      if (!isVerticallyPassable(SpigotUtil.getBlock(origin.atOffset(0, -1, 1)))) {
        Door doorBlock = (Door) block;
        if (block.getMaterial().equals(Material.IRON_DOOR)) {
          // Need to check if the door is blocking
          if (doorBlock.getFacing().equals(BlockFace.EAST)
              || doorBlock.getFacing().equals(BlockFace.WEST)
              || doorBlock.isOpen()) {
            // Nothing blocking
            accept(origin.atOffset(0, 0, 1), 1.0d, options);
          } else {
            // We need to be able to open the door
            if (MaterialGroups.PRESSURE_PLATES.contains(SpigotUtil.getBlock(origin).getMaterial())) {
              // We can step on a pressure plate to open it
              accept(origin.atOffset(0, 0, 1), 1.0d, options);
            } else {
              reject(origin.atOffset(0, 0, 1));
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          accept(origin.atOffset(0, 0, 1), 1.0d, options);
        }
      } else {
        reject(origin.atOffset(0, -1, 1));
      }
    } else {
      reject(cell);
    }

    // Neg X - West
    cell = origin.atOffset(-1, 0, 0);
    block = SpigotUtil.getBlock(cell);
    // Check if we found a door
    if (block instanceof Door) {
      // Check it's a solid floor
      if (!isVerticallyPassable(SpigotUtil.getBlock(origin.atOffset(-1, -1, 0)))) {
        Door doorBlock = (Door) block;
        if (block.getMaterial().equals(Material.IRON_DOOR)) {
          // Need to check if the door is blocking
          if (doorBlock.getFacing().equals(BlockFace.NORTH)
              || doorBlock.getFacing().equals(BlockFace.SOUTH)
              || doorBlock.isOpen()) {
            // Nothing blocking
            accept(origin.atOffset(-1, 0, 0), 1.0d, options);
          } else {
            // We need to be able to open the door
            if (MaterialGroups.PRESSURE_PLATES.contains(SpigotUtil.getBlock(origin).getMaterial())) {
              // We can step on a pressure plate to open it
              accept(origin.atOffset(-1, 0, 0), 1.0d, options);
            } else {
              reject(origin.atOffset(-1, 0, 0));
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          accept(origin.atOffset(-1, 0, 0), 1.0d, options);
        }
      } else {
        reject(origin.atOffset(-1, -1, 0));
      }
    } else {
      reject(cell);
    }

    // Neg Z - South
    cell = origin.atOffset(0, 0, -1);
    block = SpigotUtil.getBlock(cell);
    // Check if we found a door
    if (block instanceof Door) {
      // Check it's a solid floor
      if (!isVerticallyPassable(SpigotUtil.getBlock(origin.atOffset(0, -1, -1)))) {
        Door doorBlock = (Door) block;
        if (block.getMaterial().equals(Material.IRON_DOOR)) {
          // Need to check if the door is blocking
          if (doorBlock.getFacing().equals(BlockFace.EAST)
              || doorBlock.getFacing().equals(BlockFace.WEST)
              || doorBlock.isOpen()) {
            // Nothing blocking
            accept(origin.atOffset(0, 0, -1), 1.0d, options);
          } else {
            // We need to be able to open the door
            if (MaterialGroups.PRESSURE_PLATES.contains(SpigotUtil.getBlock(origin).getMaterial())) {
              // We can step on a pressure plate to open it
              accept(origin.atOffset(0, 0, -1), 1.0d, options);
            } else {
              reject(origin.atOffset(0, 0, -1));
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          accept(origin.atOffset(0, 0, -1), 1.0d, options);
        }
      } else {
        reject(origin.atOffset(0, -1, -1));
      }
    } else {
      reject(cell);
    }

  }

  @Override
  public @NotNull ModeType type() {
    return ModeType.DOOR;
  }
}
