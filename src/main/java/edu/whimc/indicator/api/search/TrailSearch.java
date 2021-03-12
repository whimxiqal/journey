package edu.whimc.indicator.api.search;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.api.path.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class TrailSearch<T extends Locatable<T, D>, D> {

  public static final int MAX_SIZE = 100000;

  @Setter
  private Consumer<Step<T, D>> visitationCallback = loc -> {};

  @Setter
  private Consumer<Step<T, D>> stepCallback = loc -> {};

  public Trail<T, D> findShortestTrail(T origin, T destination, Collection<Mode<T, D>> modes)
      throws MemoryCapacityException {
    if (!origin.getDomain().equals(destination.getDomain())) {
      return null;  // These must have the same domain!
    }
    Queue<Node> nexts = new PriorityQueue<>(Comparator.comparingDouble(Node::getProximity));
    Map<T, Node> visited = new HashMap<>();

    Node originNode = new Node(new Step<>(origin, ModeType.NONE), null, origin.distanceTo(destination));
    nexts.add(originNode);
    visited.put(origin, originNode);
    visitationCallback.accept(originNode.getData());

    Node current;
    while (!nexts.isEmpty()) {
      if (visited.size() > MAX_SIZE) {
        throw new MemoryCapacityException(String.format(
            "The path finding algorithm used too much memory: %d elements",
            visited.size()));  // Too large, couldn't find a solution
      }

      current = nexts.poll();
      stepCallback.accept(current.getData());

      // We found it!
      if (current.getData().getLocatable().equals(destination)) {
        double length = current.getScore();
        LinkedList<Step<T, D>> steps = new LinkedList<>();
        do {
          steps.addFirst(current.getData());
          current = current.getPrevious();
        } while (current != null);
        return new Trail<>(steps, length);
      }

      // Need to keep going
      for (Mode<T, D> mode : modes) {
        for (Map.Entry<T, Float> next : mode.getDestinations(current.getData().getLocatable()).entrySet()) {
          if (visited.containsKey(next.getKey())) {
            // Already visited, but see if it is better to come from this new direction
            if (current.getScore() + next.getValue() < visited.get(next.getKey()).getScore()) {
              Indicator.getInstance().getLogger().info("Score was updated at " + next.getKey().print());
              visited.get(next.getKey()).setPrevious(current);
              visited.get(next.getKey()).setScore(current.getScore() + next.getValue());
              visited.get(next.getKey()).getData().setModeType(mode.getType());
            }
          } else {
            // Not visited. Set up node, give it a score, and add it to the system
            Node nextNode = new Node(new Step<>(next.getKey(), mode.getType()), current, next.getKey().distanceTo(destination));
            nextNode.setScore(current.getScore() + next.getValue());
            nexts.add(nextNode);
            visited.put(next.getKey(), nextNode);
            visitationCallback.accept(nextNode.getData());
          }
        }
      }
    }

    return null;  // Nothing found
  }

  class Node {
    @Getter private final Step<T, D> data;
    @Getter private final double proximity;
    @Getter @Setter private Node previous;
    @Getter @Setter private double score = Double.MAX_VALUE;

    public Node(@NotNull Step<T, D> data, Node previous, double proximity) {
      this.data = data;
      this.previous = previous;
      this.proximity = proximity;
    }

  }

  static class MemoryCapacityException extends RuntimeException {
    public MemoryCapacityException(String msg) {
      super(msg);
    }
  }

}
