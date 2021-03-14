package edu.whimc.indicator.api.search;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.whimc.indicator.api.path.*;

import java.util.*;

public class PathEdgeGraph<T extends Locatable<T, D>, D> {

    private final Table<Node, Node, Trail<T, D>> edges = HashBasedTable.create();

    public void addEdge(Node origin, Node destination, Trail<T, D> trail) {
      this.edges.put(origin, destination, trail);
    }

    public static class Node {
      private double distance = Double.MAX_VALUE;
      private Node previous;
      private Node() {
      }
    }

    private class LinkNode extends Node {
      private final Link<T, D> link;
      public LinkNode(Link<T, D> link) {
        this.link = link;
      }

      public Link<T, D> getLink() {
        return link;
      }
    }

    public Node generateNode() {
      return new Node();
    }

    public Node generateLinkNode(Link<T, D> link) {
      return new LinkNode(link);
    }

    @SuppressWarnings("unchecked")
    public Path<T, D> findMinimumPath(Node origin, Node destination) {
      Queue<Node> toVisit = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));
      Set<Node> visited = new HashSet<>();

      origin.distance = 0;
      origin.previous = null;
      visited.add(origin);
      edges.row(origin).forEach((node, path) -> {
        node.distance = path.getLength();
        node.previous = origin;
        toVisit.add(node);
      });

      Node current;
      while (!toVisit.isEmpty()) {
        current = toVisit.poll();
        if (current.equals(destination)) {
          // Backwards traverse to get the correct path, then
          //  backwards traverse again to put it back in the correct
          //  order.
          Stack<Trail<T, D>> trails = new Stack<>();
          Stack<Link<T, D>> links = new Stack<>();
          while (current.previous != null) {
            trails.add(edges.get(current.previous, current));
            if (current instanceof PathEdgeGraph.LinkNode) {
              links.add(((PathEdgeGraph<T, D>.LinkNode) current).getLink());
            }
            current = current.previous;
          }
          Path<T, D> path = new Path<>();
          while (!links.isEmpty()) {
            if (!path.addLinkedTrail(trails.pop(), links.pop())) {
              throw new RuntimeException("Could not add linked trail to path");
            }
          }
          if (!path.addFinalTrail(trails.pop())) {
            throw new RuntimeException("Could not add final trail");
          }
          assert trails.isEmpty();
          assert links.isEmpty();
          return path;
        }

        // Not yet done
        for (Map.Entry<Node, Trail<T, D>> outlet : edges.row(current).entrySet()) {
          if (visited.contains(outlet.getKey())) {
            continue;
          }
          if (outlet.getKey().distance > current.distance + outlet.getValue().getLength()) {
            // A better path for this node would be to come from current.
            // We can assume that is already queued. Remove from waiting queue to update.
            toVisit.remove(outlet.getKey());
          }
          outlet.getKey().distance = current.distance + outlet.getValue().getLength();
          outlet.getKey().previous = current;
          toVisit.add(outlet.getKey());
        }
      }

      return null;  // Could not find it

    }

}
