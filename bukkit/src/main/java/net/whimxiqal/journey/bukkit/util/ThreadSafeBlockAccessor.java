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

package net.whimxiqal.journey.bukkit.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.bukkit.JourneyBukkit;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

public class ThreadSafeBlockAccessor {

  private static final long CHUNK_SNAPSHOT_LIFETIME_MS = 10000L;
  private static final long MAX_CACHED_CHUNKS = 1024;

  ChunkMap chunkMap = new ChunkMap();                     // synchronized
  Queue<ChunkItem> chunkList = new LinkedList<>();        // synchronized
  Queue<ChunkRequest> requestQueue = new LinkedList<>();  // synchronized
  BukkitTask task;

  public void init() {
    task = Bukkit.getScheduler().runTaskTimer(JourneyBukkit.get(), () -> {
      synchronized (this) {
        long timeStampThreshold = System.currentTimeMillis() - CHUNK_SNAPSHOT_LIFETIME_MS;
        while (!chunkList.isEmpty()) {
          ChunkItem item = chunkList.peek();
          if (item.timeMs < timeStampThreshold) {
            chunkMap.drop(item.domain, item.chunk);
            chunkList.remove();
          } else {
            break;
          }
        }

        Map<ChunkRequest.Data, ChunkSnapshot> completedChunkRequests = new HashMap<>();
        while (!requestQueue.isEmpty() && chunkMap.size() <= MAX_CACHED_CHUNKS) {
          ChunkRequest req = requestQueue.remove();
          ChunkSnapshot existingSnapshot = completedChunkRequests.get(req.data);
          if (existingSnapshot != null) {
            req.future.complete(existingSnapshot);
            continue;
          }
          World world = BukkitUtil.getWorld(req.data.domain);
          ChunkSnapshot snapshot = world.getChunkAt(req.data.x, req.data.z).getChunkSnapshot();
          chunkMap.save(req.data.domain, snapshot);
          chunkList.add(new ChunkItem(req.data.domain, snapshot));
          completedChunkRequests.put(req.data, snapshot);
          req.future.complete(snapshot);
        }
      }
    }, 0, 1);
    Journey.get().proxy().logger().info("ThreadSafeBlockAccessor initialized");
  }

  public void shutdown() {
    if (task != null) {
      task.cancel();
    }
  }

  public BlockData getBlock(Cell cell) {
    if (Bukkit.isPrimaryThread()) {
      throw new RuntimeException("This method was called from the primary thread");  // programmer error
    }
    ChunkRequest request;
    synchronized (this) {
      BlockData data = chunkMap.get(cell.domain(), cell.blockX(), cell.blockY(), cell.blockZ());
      if (data != null) {
        return data;
      }
      // data is not in lookup map, queue chunk
      request = new ChunkRequest(cell.domain(), Math.floorDiv(cell.blockX(), 16), Math.floorDiv(cell.blockZ(), 16));
      requestQueue.add(request);
    }

    // Wait for request to be completed
    ChunkSnapshot snapshot = BukkitUtil.waitUntil(request.future);
    return snapshot.getBlockData(Math.floorMod(cell.blockX(), 16), cell.blockY(), Math.floorMod(cell.blockZ(), 16));
  }

  private static class ChunkItem {
    final int domain;
    final ChunkSnapshot chunk;
    final long timeMs;
    ChunkItem(int domain, ChunkSnapshot chunk) {
      this.domain = domain;
      this.chunk = chunk;
      this.timeMs = System.currentTimeMillis();
    }
  }

  private static class ChunkRequest {

    private static class Data {
      final int domain;
      final int x;
      final int z;
      Data(int domain, int x, int z) {
        this.domain = domain;
        this.x = x;
        this.z = z;
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data that = (Data) o;
        return x == that.x && z == that.z && domain == that.domain;
      }

      @Override
      public int hashCode() {
        return Objects.hash(domain, x, z);
      }

    }
    final Data data;
    final CompletableFuture<ChunkSnapshot> future;
    ChunkRequest(int domain, int x, int z) {
      this.data = new Data(domain, x, z);
      this.future = new CompletableFuture<>();
    }

    @Override
    public String toString() {
      return "ChunkRequest{" +
          "domainId='" + data.domain + '\'' +
          ", x=" + data.x +
          ", z=" + data.z +
          '}';
    }
  }

  /**
   * mapping domain -> (x -> (z -> chunk))
   */
  private static class ChunkMap extends HashMap<Integer, Map<Integer, Map<Integer, ChunkSnapshot>>> {

    @Nullable
    public BlockData get(int domain, int x, int y, int z) {
      Map<Integer, Map<Integer, ChunkSnapshot>> chunkCoordinates = get(domain);
      if (chunkCoordinates == null) {
        return null;
      }
      Map<Integer, ChunkSnapshot> chunkCoordinates2 = chunkCoordinates.get(Math.floorDiv(x, 16));
      if (chunkCoordinates2 == null) {
        return null;
      }
      ChunkSnapshot snapshot = chunkCoordinates2.get(Math.floorDiv(z, 16));
      if (snapshot == null) {
        return null;
      }
      return snapshot.getBlockData(Math.floorMod(x, 16), y, Math.floorMod(z, 16));
    }

    private void save(int domain, ChunkSnapshot snapshot) {
      Map<Integer, Map<Integer, ChunkSnapshot>> chunkCoordinates = computeIfAbsent(domain, k -> new HashMap<>());
      Map<Integer, ChunkSnapshot> chunkCoordinates2 = chunkCoordinates.computeIfAbsent(snapshot.getX(), k -> new HashMap<>());
      chunkCoordinates2.put(snapshot.getZ(), snapshot);
    }

    public void drop(int domain, ChunkSnapshot chunkSnapshot) {
      Map<Integer, Map<Integer, ChunkSnapshot>> chunkCoordinates = get(domain);
      if (chunkCoordinates == null) {
        throw new IllegalArgumentException("No chunk snapshots found with world " + chunkSnapshot.getWorldName());
      }
      Map<Integer, ChunkSnapshot> chunkCoordinates2 = chunkCoordinates.get(chunkSnapshot.getX());
      if (chunkCoordinates2 == null) {
        throw new IllegalArgumentException("No chunk snapshots found in world " + chunkSnapshot.getWorldName() + " with X: " + chunkSnapshot.getX());
      }
      ChunkSnapshot snapshot = chunkCoordinates2.remove(chunkSnapshot.getZ());
      if (snapshot == null) {
        throw new IllegalArgumentException("No chunk snapshots found in world " + chunkSnapshot.getWorldName() + " with X: " + chunkSnapshot.getX() + ", Z: " + chunkSnapshot.getZ());
      }
    }
  }

  private static String printChunk(ChunkSnapshot chunk) {
    return "Chunk{" + chunk.getX() + "," + chunk.getZ() + "}";
  }

}
