package edu.whimc.indicator.api.search;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.api.path.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class PathEdgeGraph<T extends Locatable<T, D>, D> {

  private final Table<Node, Node, Trail<T, D>> edges = HashBasedTable.create();

  public void addEdge(Node origin, Node destination, Trail<T, D> trail) {
    this.edges.put(origin, destination, trail);
  }

  public static class Node {
    @Getter @Setter
    private double distance;
    @Setter @Getter
    private Node previous;
    /**
     * The "weight" of the node, which is the cost of traversing through this node.
     */
    @Getter
    protected double weight = 0;

    private Node(double distance) {
      this.distance = distance;
    }
  }

  private class LinkNode extends Node {
    private final Link<T, D> link;

    public LinkNode(Link<T, D> link, double distance) {
      super(distance);
      this.link = link;
      weight = link.weight();
    }

    public Link<T, D> getLink() {
      return link;
    }
  }

  public Node generateNode() {
    return new Node(Double.MAX_VALUE);
  }

  public Node generateLinkNode(Link<T, D> link) {
    return new LinkNode(link, Double.MAX_VALUE);
  }

  @SuppressWarnings("unchecked")
  public Path<T, D> findMinimumPath(Node origin, Node destination) {
    PriorityQueue<Node> toVisit = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));
    Set<Node> visited = new HashSet<>();

    origin.setDistance(0);
    origin.setPrevious(null);
    toVisit.add(origin);
//      edges.row(origin).forEach((node, trail) -> {
//        node.distance = trail.getLength();
//        node.previous = origin;
//        toVisit.add(node);
//      });

    Node current;
    while (!toVisit.isEmpty()) {
      current = toVisit.poll();
      visited.add(current);

      if (current.equals(destination)) {
        // Backwards traverse to get the correct path, then
        //  backwards traverse again to put it back in the correct
        //  order.
        Stack<Trail<T, D>> trails = new Stack<>();
        Stack<Link<T, D>> links = new Stack<>();
        while (current.getPrevious() != null) {
          trails.add(edges.get(current.getPrevious(), current));
          if (current instanceof PathEdgeGraph.LinkNode) {
            links.add(((PathEdgeGraph<T, D>.LinkNode) current).getLink());
          }
          current = current.getPrevious();
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
        if (outlet.getKey().getDistance() > current.getDistance() + outlet.getValue().getLength()) {
          // A better path for this node would be to come from current.
          // We can assume that is already queued. Remove from waiting queue to update.
          toVisit.remove(outlet.getKey());
          outlet.getKey().setDistance(current.getDistance() + outlet.getValue().getLength() + outlet.getKey().getWeight());
          outlet.getKey().setPrevious(current);
          toVisit.add(outlet.getKey());
        }
      }
    }

    return null;  // Could not find it

  }

}
