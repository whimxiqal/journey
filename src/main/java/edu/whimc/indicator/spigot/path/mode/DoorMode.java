package edu.whimc.indicator.spigot.path.mode;

import edu.whimc.indicator.common.path.Mode;
import edu.whimc.indicator.common.path.ModeType;
import edu.whimc.indicator.common.path.ModeTypes;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.util.MaterialGroups;
import edu.whimc.indicator.spigot.util.SpigotUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;

import java.util.HashMap;
import java.util.Map;

public class DoorMode implements Mode<LocationCell, World> {

  @Override
  public Map<LocationCell, Double> getDestinations(LocationCell origin) {
    Map<LocationCell, Double> doorFloors = new HashMap<>();
    // Pos X - East
    Block block = origin.getBlockAtOffset(1, 0, 0);
    // Check if we found a door
    if (block.getBlockData() instanceof Door) {
      // Check it's a solid floor
      if (!SpigotUtil.isVerticallyPassable(origin.getBlockAtOffset(1, -1, 0))) {
        Door doorBlock = (Door) block.getBlockData();
        if (block.getType().equals(Material.IRON_DOOR)) {
          // Need to check if the door is blocking
          if (doorBlock.getFacing().equals(BlockFace.NORTH)
              || doorBlock.getFacing().equals(BlockFace.SOUTH)
              || doorBlock.isOpen()) {
            // Nothing blocking
            doorFloors.put(origin.createLocatableAtOffset(1, 0, 0), 1.0d);
          } else {
            // We need to be able to open the door
            if (MaterialGroups.PRESSURE_PLATES.contains(origin.getBlock().getType())) {
              // We can step on a pressure plate to open it
              doorFloors.put(origin.createLocatableAtOffset(1, 0, 0), 1.0d);
            } else {
              // TODO Find any buttons or levers nearby
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          doorFloors.put(origin.createLocatableAtOffset(1, 0, 0), 1d);
        }
      }
    }

    // Pos Z - North
    block = origin.getBlockAtOffset(0, 0, 1);
    // Check if we found a door
    if (block.getBlockData() instanceof Door) {
      // Check it's a solid floor
      if (!SpigotUtil.isVerticallyPassable(origin.getBlockAtOffset(0, -1, 1))) {
        Door doorBlock = (Door) block.getBlockData();
        if (block.getType().equals(Material.IRON_DOOR)) {
          // Need to check if the door is blocking
          if (doorBlock.getFacing().equals(BlockFace.EAST)
              || doorBlock.getFacing().equals(BlockFace.WEST)
              || doorBlock.isOpen()) {
            // Nothing blocking
            doorFloors.put(origin.createLocatableAtOffset(0, 0, 1), 1.0d);
          } else {
            // We need to be able to open the door
            if (MaterialGroups.PRESSURE_PLATES.contains(origin.getBlock().getType())) {
              // We can step on a pressure plate to open it
              doorFloors.put(origin.createLocatableAtOffset(0, 0, 1), 1.0d);
            } else {
              // TODO Find any buttons or levers nearby
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          doorFloors.put(origin.createLocatableAtOffset(0, 0, 1), 1d);
        }
      }
    }

    // Neg X - West
    block = origin.getBlockAtOffset(-1, 0, 0);
    // Check if we found a door
    if (block.getBlockData() instanceof Door) {
      // Check it's a solid floor
      if (!SpigotUtil.isVerticallyPassable(origin.getBlockAtOffset(-1, -1, 0))) {
        Door doorBlock = (Door) block.getBlockData();
        if (block.getType().equals(Material.IRON_DOOR)) {
          // Need to check if the door is blocking
          if (doorBlock.getFacing().equals(BlockFace.NORTH)
              || doorBlock.getFacing().equals(BlockFace.SOUTH)
              || doorBlock.isOpen()) {
            // Nothing blocking
            doorFloors.put(origin.createLocatableAtOffset(-1, 0, 0), 1.0d);
          } else {
            // We need to be able to open the door
            if (MaterialGroups.PRESSURE_PLATES.contains(origin.getBlock().getType())) {
              // We can step on a pressure plate to open it
              doorFloors.put(origin.createLocatableAtOffset(-1, 0, 0), 1.0d);
            } else {
              // TODO Find any buttons or levers nearby
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          doorFloors.put(origin.createLocatableAtOffset(-1, 0, 0), 1d);
        }
      }
    }

    // Neg Z - South
    block = origin.getBlockAtOffset(0, 0, -1);
    // Check if we found a door
    if (block.getBlockData() instanceof Door) {
      // Check it's a solid floor
      if (!SpigotUtil.isVerticallyPassable(origin.getBlockAtOffset(0, -1, -1))) {
        Door doorBlock = (Door) block.getBlockData();
        if (block.getType().equals(Material.IRON_DOOR)) {
          // Need to check if the door is blocking
          if (doorBlock.getFacing().equals(BlockFace.EAST)
              || doorBlock.getFacing().equals(BlockFace.WEST)
              || doorBlock.isOpen()) {
            // Nothing blocking
            doorFloors.put(origin.createLocatableAtOffset(0, 0, -1), 1.0d);
          } else {
            // We need to be able to open the door
            if (MaterialGroups.PRESSURE_PLATES.contains(origin.getBlock().getType())) {
              // We can step on a pressure plate to open it
              doorFloors.put(origin.createLocatableAtOffset(0, 0, -1), 1.0d);
            } else {
              // TODO Find any buttons or levers nearby
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          doorFloors.put(origin.createLocatableAtOffset(0, 0, -1), 1d);
        }
      }
    }

    return doorFloors;
  }

  @Override
  public ModeType getType() {
    return ModeTypes.DOOR;
  }
}
