package net.whimxiqal.journey.bukkit.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import net.whimxiqal.journey.common.Journey;
import net.whimxiqal.journey.common.navigation.Cell;
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

  ChunkMap chunkMap = new ChunkMap();                     // protected by lock
  Queue<ChunkItem> chunkList = new LinkedList<>();        // protected by lock
  Queue<ChunkRequest> requestQueue = new LinkedList<>();  // protected by lock
  ReentrantLock lock = new ReentrantLock();
  BukkitTask task;

  public void init() {
    task = Bukkit.getScheduler().runTaskTimer(JourneyBukkit.getInstance(), () -> {
      lock.lock();
      long timeStampThreshold = System.currentTimeMillis() - CHUNK_SNAPSHOT_LIFETIME_MS;
      while (!chunkList.isEmpty()) {
        ChunkItem item = chunkList.peek();
        if (item.timeMs < timeStampThreshold) {
          chunkMap.drop(item.domainId, item.chunk);
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
        World world = BukkitUtil.getWorld(req.data.domainId);
        ChunkSnapshot snapshot = world.getChunkAt(req.data.x, req.data.z).getChunkSnapshot();
        chunkMap.save(req.data.domainId, snapshot);
        chunkList.add(new ChunkItem(req.data.domainId, snapshot));
        completedChunkRequests.put(req.data, snapshot);
        req.future.complete(snapshot);
      }
      lock.unlock();
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
    lock.lock();
    BlockData data = chunkMap.get(cell.domainId(), cell.getX(), cell.getY(), cell.getZ());
    if (data != null) {
      lock.unlock();
      return data;
    }
    // data is not in lookup map, queue chunk
    ChunkRequest request = new ChunkRequest(cell.domainId(), Math.floorDiv(cell.getX(), 16), Math.floorDiv(cell.getZ(), 16));
    requestQueue.add(request);
    lock.unlock();

    // Wait for request to be completed
    ChunkSnapshot snapshot = BukkitUtil.waitUntil(request.future);
    return snapshot.getBlockData(Math.floorMod(cell.getX(), 16), cell.getY(), Math.floorMod(cell.getZ(), 16));
  }

  private static class ChunkItem {
    final String domainId;
    final ChunkSnapshot chunk;
    final long timeMs;
    ChunkItem(String domainId, ChunkSnapshot chunk) {
      this.domainId = domainId;
      this.chunk = chunk;
      this.timeMs = System.currentTimeMillis();
    }
  }

  private static class ChunkRequest {

    private static class Data {
      final String domainId;
      final int x;
      final int z;
      Data(String domainId, int x, int z) {
        this.domainId = domainId;
        this.x = x;
        this.z = z;
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data that = (Data) o;
        return x == that.x && z == that.z && domainId.equals(that.domainId);
      }

      @Override
      public int hashCode() {
        return Objects.hash(domainId, x, z);
      }

    }
    final Data data;
    final CompletableFuture<ChunkSnapshot> future;
    ChunkRequest(String domainId, int x, int z) {
      this.data = new Data(domainId, x, z);
      this.future = new CompletableFuture<>();
    }

    @Override
    public String toString() {
      return "ChunkRequest{" +
          "domainId='" + data.domainId + '\'' +
          ", x=" + data.x +
          ", z=" + data.z +
          '}';
    }
  }

  private static class ChunkMap extends HashMap<String, Map<Integer, Map<Integer, ChunkSnapshot>>> {

    @Nullable
    public BlockData get(String domainId, int x, int y, int z) {
      Map<Integer, Map<Integer, ChunkSnapshot>> chunkCoordinates = get(domainId);
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

    private void save(String domainId, ChunkSnapshot snapshot) {
      Map<Integer, Map<Integer, ChunkSnapshot>> chunkCoordinates = computeIfAbsent(domainId, k -> new HashMap<>());
      Map<Integer, ChunkSnapshot> chunkCoordinates2 = chunkCoordinates.computeIfAbsent(snapshot.getX(), k -> new HashMap<>());
      chunkCoordinates2.put(snapshot.getZ(), snapshot);
    }

    public void drop(String domainId, ChunkSnapshot chunkSnapshot) {
      Map<Integer, Map<Integer, ChunkSnapshot>> chunkCoordinates = get(domainId);
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
