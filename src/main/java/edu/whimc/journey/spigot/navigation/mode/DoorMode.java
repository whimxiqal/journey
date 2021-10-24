package edu.whimc.journey.spigot.navigation.mode;

import edu.whimc.journey.common.navigation.ModeType;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.util.MaterialGroups;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;

public final class DoorMode extends SpigotMode {

  public DoorMode(Set<Material> forcePassable) {
    super(forcePassable);
  }

  @Override
  public void collectDestinations(LocationCell origin) {
    LocationCell cell;
    Block block;
    // Pos X - East
    cell = origin.createLocatableAtOffset(1, 0, 0);
    block = cell.getBlock();
    // Check if we found a door
    if (block.getBlockData() instanceof Door) {
      // Check it's a solid floor
      if (!isVerticallyPassable(origin.getBlockAtOffset(1, -1, 0))) {
        Door doorBlock = (Door) block.getBlockData();
        if (block.getType().equals(Material.IRON_DOOR)) {
          // Need to check if the door is blocking
          if (doorBlock.getFacing().equals(BlockFace.NORTH)
              || doorBlock.getFacing().equals(BlockFace.SOUTH)
              || doorBlock.isOpen()) {
            // Nothing blocking
            accept(origin.createLocatableAtOffset(1, 0, 0), 1.0d);
          } else {
            // We need to be able to open the door
            if (MaterialGroups.PRESSURE_PLATES.contains(origin.getBlock().getType())) {
              // We can step on a pressure plate to open it
              accept(origin.createLocatableAtOffset(1, 0, 0), 1.0d);
            } else if (false) {
              // TODO Find any buttons or levers nearby
            } else {
              reject(origin.createLocatableAtOffset(1, 0, 0));
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          accept(origin.createLocatableAtOffset(1, 0, 0), 1d);
        }
      } else {
        reject(origin.createLocatableAtOffset(1, -1, 0));
      }
    } else {
      reject(new LocationCell(block.getLocation()));
    }

    // Pos Z - North
    cell = origin.createLocatableAtOffset(0, 0, 1);
    block = cell.getBlock();
    // Check if we found a door
    if (block.getBlockData() instanceof Door) {
      // Check it's a solid floor
      if (!isVerticallyPassable(origin.getBlockAtOffset(0, -1, 1))) {
        Door doorBlock = (Door) block.getBlockData();
        if (block.getType().equals(Material.IRON_DOOR)) {
          // Need to check if the door is blocking
          if (doorBlock.getFacing().equals(BlockFace.EAST)
              || doorBlock.getFacing().equals(BlockFace.WEST)
              || doorBlock.isOpen()) {
            // Nothing blocking
            accept(origin.createLocatableAtOffset(0, 0, 1), 1.0d);
          } else {
            // We need to be able to open the door
            if (MaterialGroups.PRESSURE_PLATES.contains(origin.getBlock().getType())) {
              // We can step on a pressure plate to open it
              accept(origin.createLocatableAtOffset(0, 0, 1), 1.0d);
            } else if (false) {
              // TODO Find any buttons or levers nearby
            } else {
              reject(origin.createLocatableAtOffset(1, 0, 0));
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          accept(origin.createLocatableAtOffset(0, 0, 1), 1d);
        }
      } else {
        reject(origin.createLocatableAtOffset(1, -1, 0));
      }
    } else {
      reject(new LocationCell(block.getLocation()));
    }

    // Neg X - West
    cell = origin.createLocatableAtOffset(-1, 0, 0);
    block = cell.getBlock();
    // Check if we found a door
    if (block.getBlockData() instanceof Door) {
      // Check it's a solid floor
      if (!isVerticallyPassable(origin.getBlockAtOffset(-1, -1, 0))) {
        Door doorBlock = (Door) block.getBlockData();
        if (block.getType().equals(Material.IRON_DOOR)) {
          // Need to check if the door is blocking
          if (doorBlock.getFacing().equals(BlockFace.NORTH)
              || doorBlock.getFacing().equals(BlockFace.SOUTH)
              || doorBlock.isOpen()) {
            // Nothing blocking
            accept(origin.createLocatableAtOffset(-1, 0, 0), 1.0d);
          } else {
            // We need to be able to open the door
            if (MaterialGroups.PRESSURE_PLATES.contains(origin.getBlock().getType())) {
              // We can step on a pressure plate to open it
              accept(origin.createLocatableAtOffset(-1, 0, 0), 1.0d);
            } else if (false) {
              // TODO Find any buttons or levers nearby
            } else {
              reject(origin.createLocatableAtOffset(1, 0, 0));
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          accept(origin.createLocatableAtOffset(-1, 0, 0), 1d);
        }
      } else {
        reject(origin.createLocatableAtOffset(1, -1, 0));
      }
    } else {
      reject(new LocationCell(block.getLocation()));
    }

    // Neg Z - South
    cell = origin.createLocatableAtOffset(0, 0, -1);
    block = cell.getBlock();
    // Check if we found a door
    if (block.getBlockData() instanceof Door) {
      // Check it's a solid floor
      if (!isVerticallyPassable(origin.getBlockAtOffset(0, -1, -1))) {
        Door doorBlock = (Door) block.getBlockData();
        if (block.getType().equals(Material.IRON_DOOR)) {
          // Need to check if the door is blocking
          if (doorBlock.getFacing().equals(BlockFace.EAST)
              || doorBlock.getFacing().equals(BlockFace.WEST)
              || doorBlock.isOpen()) {
            // Nothing blocking
            accept(origin.createLocatableAtOffset(0, 0, -1), 1.0d);
          } else {
            // We need to be able to open the door
            if (MaterialGroups.PRESSURE_PLATES.contains(origin.getBlock().getType())) {
              // We can step on a pressure plate to open it
              accept(origin.createLocatableAtOffset(0, 0, -1), 1.0d);
            } else if (false) {
              // TODO Find any buttons or levers nearby
            } else {
              reject(origin.createLocatableAtOffset(1, 0, 0));
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          accept(origin.createLocatableAtOffset(0, 0, -1), 1d);
        }
      } else {
        reject(origin.createLocatableAtOffset(1, -1, 0));
      }
    } else {
      reject(new LocationCell(block.getLocation()));
    }

  }

  @Override
  public ModeType getType() {
    return ModeType.DOOR;
  }
}
