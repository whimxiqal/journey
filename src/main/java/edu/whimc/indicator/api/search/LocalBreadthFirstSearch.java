package edu.whimc.indicator.api.search;

import edu.whimc.indicator.util.Printable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LocalBreadthFirstSearch<T extends Locatable<T, D>, D> {

  public static final int MAX_SIZE = 10000;

  public List<T> findShortestPath(T origin, T destination, Collection<Mode<T, D>> modes) {
    System.out.println("Finding Shortest Path!");
    if (!origin.getDomain().equals(destination.getDomain())) {
      return null;  // These must have the same domain!
    }
    Queue<Node> openSet = new PriorityQueue<>((Comparator.comparingInt(Node::getScore)));
    Set<T> visited = new HashSet<>();

    Node start = new Node(origin, null, destination);
    visited.add(origin);
    openSet.add(start);

    System.out.println("Starting shortest path search");
    Node current;
    while (!openSet.isEmpty()) {
//      openSet.forEach(node -> {
//        if (node.getData() instanceof Printable) {
//          System.out.print("[");
//          ((Printable) node.getData()).print();
//          System.out.printf(", %d],", node.getData().distanceTo(destination));
//        }
//      });
//      System.out.print("\n");
      if (openSet.size() + visited.size() > MAX_SIZE) {
        return null;  // Too large, couldn't find a solution
      }

      current = openSet.poll();

      // We found it!
      if (current.getData().equals(destination)) {
        System.out.println("We found a match");
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
            openSet.add(new Node(next, current, destination));
            visited.add(next);
          }
        }
      }
    }

    return null;  // Nothing found
  }

  class Node {
    private final T data;
    private final Node previous;
    private final int score;

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

    public int getScore() {
      return score;
    }
  }

}
