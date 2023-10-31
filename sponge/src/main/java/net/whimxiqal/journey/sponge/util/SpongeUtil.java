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

package net.whimxiqal.journey.sponge.util;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.math.Vector;
import net.whimxiqal.journey.search.flag.FlagSet;
import net.whimxiqal.journey.search.flag.Flags;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

/**
 * A utility class to handle general odd Spigot Minecraft-related operations.
 */
public final class SpongeUtil {

  /**
   * Return true if you can pass vertically through this block.
   *
   * @param state the block
   * @return false if you cannot pass through this block vertically
   */
  public static boolean isVerticallyPassable(BlockState state) {
    if (isPassable(state)) {
      return true;
    }
    return MaterialGroups.isVerticallySpecialPassable(state.type());
  }

  /**
   * Determine if a block can be laterally passed through, as in,
   * can the entity move in a lateral direction and go through that
   * block location.
   *
   * @param state the block
   * @return true if passable
   */
  public static boolean isLaterallyPassable(BlockState state) {
    if (isPassable(state)) {
      return true;
    }
    return MaterialGroups.isLaterallySpecialPassable(state.type());
  }

  /**
   * Return true if you can pass laterally through this block.
   *
   * @param state the block
   * @return false if you cannot pass through this block laterally
   */
  public static boolean isLaterallyPassable(BlockState state, FlagSet flagSet) {
    if (isPassable(state)) {
      return true;
    }
    if (state.type().equals(BlockTypes.IRON_DOOR.get()) && flagSet.getValueFor(Flags.DOOR)) {
      return true;
    }
    return MaterialGroups.isLaterallySpecialPassable(state.type());
  }

  /**
   * Determine if this block is generally passable.
   *
   * @param state the block
   * @return true if passable
   */
  public static boolean isPassable(BlockState state) {
    // NOTE: snow is unhandled
    return state.getOrElse(Keys.IS_PASSABLE, false);
  }

  /**
   * Can a player be supported by this block below him.
   *
   * @param state the block
   * @return false if a player cannot stand on top of the block
   */
  public static boolean canStandOn(BlockState state) {
    return !isPassable(state) || state.type().equals(BlockTypes.LADDER.get());
  }

  /**
   * Can a player be supported by this block within the same block as him.
   *
   * @param state the block
   * @return false if a player cannot stand within this block and be supported
   */
  public static boolean canStandIn(BlockState state) {
    return isLaterallyPassable(state) && !isVerticallyPassable(state);
  }

  public static int getDomain(ServerWorld world) {
    return Journey.get().domainManager().domainIndex(getWorldId(world));
  }

  public static UUID getWorldId(ServerWorld world) {
    return world.uniqueId();
  }

  public static Cell toCell(ServerLocation location) {
    return new Cell(location.blockX(), location.blockY(), location.blockZ(), getDomain(location.world()));
  }

  public static Cell toCell(ServerWorld world, Vector3d position) {
    return new Cell(position.floorX(), position.floorY(), position.floorZ(), getDomain(world));
  }

  public static ServerWorld getWorld(Cell cell) {
    return getWorld(Journey.get().domainManager().domainId(cell.domain()));
  }

  public static ServerWorld getWorld(UUID domainId) {
    Optional<ServerWorld> world = Sponge.server().worldManager().worldKey(domainId)
        .flatMap(key -> Sponge.server().worldManager().world(key));
    if (world.isEmpty()) {
      throw new IllegalArgumentException("There is no world with id " + domainId);
    }
    return world.get();
  }

  public static ServerWorld getWorld(int domain) {
    return getWorld(Journey.get().domainManager().domainId(domain));
  }

  /**
   * Get the Bukkit block data at the given location.
   * Must be called on the main server thread.
   *
   * @param cell cell
   * @return the block data at the cell location
   */
  public static BlockState getBlock(Cell cell) {
    if (!Sponge.server().onMainThread()) {
      throw new IllegalThreadStateException("Calls to getBlock must be made on the main thread");
    }
    return getWorld(Journey.get().domainManager().domainId(cell.domain())).block(cell.blockX(), cell.blockY(), cell.blockZ());
  }

  public static ServerLocation toLocation(Cell cell) {
    return ServerLocation.of(getWorld(cell), cell.blockX(), cell.blockY(), cell.blockZ());
  }

  public static Vector toLocalVector(Vector3i vector) {
    return new Vector(vector.x(), vector.y(), vector.z());
  }

  public static <T> T waitUntil(Future<T> future) {
    if (Sponge.server().onMainThread()) {
      throw new RuntimeException("This was called on the main server thread");  // programmer error
    }
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      return null;
    }
  }

}
