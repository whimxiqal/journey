package edu.whimc.indicator.api.search;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class LocalBreadthFirstSearch<T extends Locatable<T, D>, D> {

  public static final int MAX_SIZE = 1000;

  @Setter
  private Consumer<T> visitationCallback = loc -> {};

  @Setter
  private Consumer<T> stepCallback = loc -> {};

  public List<T> findShortestPath(T origin, T destination, Collection<Mode<T, D>> modes)
      throws MemoryCapacityException {
    if (!origin.getDomain().equals(destination.getDomain())) {
      return null;  // These must have the same domain!
    }
    Queue<Node> nexts = new PriorityQueue<>(Comparator.comparingDouble(Node::getScore));
    Set<T> visited = new HashSet<>();

    Node start = new Node(origin, null, destination);
    nexts.add(start);
    visited.add(origin);
    visitationCallback.accept(origin);

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
      if (current.getData().equals(destination)) {
        LinkedList<T> path = new LinkedList<>();
        do {
          path.addFirst(current.getData());
          current = current.getPrevious();
        } while (current != null);
        return path;
      }

      // Need to keep going
      for (Mode<T, D> mode : modes) {
        for (T next : mode.getDestinations(current.getData())) {
          if (!visited.contains(next)) {
            nexts.add(new Node(next, current, destination));
            visited.add(next);
            visitationCallback.accept(next);
          }
        }
      }
    }

    return null;  // Nothing found
  }

  class Node {
    private final T data;
    private final Node previous;
    private final double score;

    public Node(@NotNull T data, Node previous, @NotNull T destination) {
      this.data = data;
      this.previous = previous;
      this.score = data.distanceTo(destination);
    }

    public T getData() {
      return data;
    }

    public Node getPrevious() {
      return previous;
    }

    public double getScore() {
      return score;
    }
  }

  static class MemoryCapacityException extends RuntimeException {
    public MemoryCapacityException(String msg) {
      super(msg);
    }
  }

}
