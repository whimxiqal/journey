package net.whimxiqal.journey.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.search.SearchSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnimationManager {

  private static final int MAX_ANIMATED_CELLS_PER_PLAYER = 10000;

  private final Queue<Request> requests = new ConcurrentLinkedQueue<>();
  private final Map<UUID, Set<Cell>> animatedCells = new HashMap<>(); // map of all cells that we have animated, saved for reversal
  private UUID taskId = null;

  /**
   * Thread-safe method to send animation cells for a player undergoing a given session.
   *
   * @param playerUuid  the player
   * @param sessionUuid the player's current session
   * @param cell       the cell that we want to animate
   */
  public void addAnimationCell(UUID playerUuid, UUID sessionUuid, @NotNull Cell cell) {
    requests.add(new Request(playerUuid, sessionUuid, cell));
  }

  /**
   * Thread-safe method to reset the ongoing animation for a player undergoing a given session.
   *
   * @param playerUuid  the player
   * @param sessionUuid the player's current session
   */
  public void resetAnimation(UUID playerUuid, UUID sessionUuid) {
    requests.add(new Request(playerUuid, sessionUuid, null));
  }

  public void initialize() {
    taskId = Journey.get().proxy().schedulingManager().scheduleRepeat(() -> {
      // Flush queue on every game tick
      while (!requests.isEmpty()) {
        Request request = requests.remove();

        if (request.cell == null) {
          // this is a request to undo all animated cells
          Set<Cell> animated = animatedCells.get(request.sessionUuid);
          if (animated == null) {
            // nothing animated
            continue;
          }
          Journey.get().proxy().platform().resetAnimationBlocks(request.playerUuid, animated);
          animatedCells.remove(request.sessionUuid);
          continue;
        }

        // this is a request to animate some new cells
        SearchSession session = Journey.get().searchManager().getSearch(request.playerUuid);
        if (session == null) {
          // player doesn't have a search anymore
          continue;
        }
        if (!session.uuid().equals(request.sessionUuid)) {
          // player is using a different search now
          continue;
        }
        Set<Cell> existing = animatedCells.get(request.sessionUuid);

        if (existing == null) {
          existing = new HashSet<>();
          existing.add(request.cell);
          animatedCells.put(request.sessionUuid, existing);
        } else if (existing.size() >= MAX_ANIMATED_CELLS_PER_PLAYER) {
          // This request is too large, we reach our max animated cells allowed per player/session
          continue;
        } else {
          existing.add(request.cell);
        }
        Journey.get().proxy().platform().sendAnimationBlock(request.playerUuid, request.cell);
      }
    }, false, 1);
  }

  public void shutdown() {
    if (taskId != null) {
      Journey.get().proxy().schedulingManager().cancelTask(taskId);
    }
  }

  private record Request(UUID playerUuid, UUID sessionUuid, @Nullable Cell cell) {
  }

}
