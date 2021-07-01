/*
 * Copyright 2021 Pieter Svenson
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

package edu.whimc.indicator.common.search.graph;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class WeightedGraph {

  private final Table<Node, Node, Edge> edges = HashBasedTable.create();

  public void addEdge(@NotNull Node origin, @NotNull Node destination, @NotNull Edge edge) {
    this.edges.put(origin, destination, edge);
  }

  @Data
  public static class Node {
    /**
     * The "weight" of the node, which is the cost of traversing through this node.
     */
    private final double weight;
    private double distance = Double.MAX_VALUE;
    private Node previous = null;

    public Node(double weight) {
      this.weight = weight;
    }
  }

  @Data
  public static class Edge {
    public final double length;
  }

  public static class Path {

    private final ArrayList<Node> nodes = Lists.newArrayList();
    private final ArrayList<Edge> edges = Lists.newArrayList();
    @Getter
    double length = 0;

    private Path(@NotNull List<Node> nodes, @NotNull List<Edge> edges) {
      this.nodes.addAll(nodes);
      this.edges.addAll(edges);
    }

    public static Builder builder(@NotNull Node destination) {
      return new Builder(destination);
    }

    /**
     * A builder to help build a path from the destination
     * backward to the origin.
     */
    public final static class Builder {
      private final LinkedList<Node> nodes = Lists.newLinkedList();
      private final LinkedList<Edge> edges = Lists.newLinkedList();

      public Builder(@NotNull Node destination) {
        this.nodes.addFirst(destination);
      }

      public final void addEdge(@NotNull Edge edge, @NotNull Node origin) {
        this.edges.addFirst(edge);
        this.nodes.addFirst(origin);
      }

      public final Path build() {
        return new Path(nodes, edges);
      }
    }

    public static class Traversal {
      private final Path path;

      private Traversal(Path path) {
        this.path = path;
      }

      private abstract static class TraversalStep {
      }

      private class NodeTraversalStep extends TraversalStep {
        private final Path path;
        private final int index;

        NodeTraversalStep(Path path, int index) {
          this.path = path;
          this.index = index;
        }

        Node getNode() {
          return path.nodes.get(index);
        }

        boolean hasNext() {
          return index < path.edges.size();
        }

        EdgeTraversalStep next() {
          return new EdgeTraversalStep(path, index);
        }
      }

      private class EdgeTraversalStep extends TraversalStep {
        private final Path path;
        private int index;

        EdgeTraversalStep(Path path, int index) {
          this.path = path;
        }

        Edge getEdge() {
          return path.edges.get(index);
        }

        NodeTraversalStep next() {
          return new NodeTraversalStep(path, index + 1);
        }
      }

      NodeTraversalStep begin() {
        return new NodeTraversalStep(path, 0);
      }

    }

    public final Node getOrigin() {
      return nodes.get(0);
    }

    public final Node getDestination() {
      return nodes.get(nodes.size() - 1);
    }

    public final Traversal traverse() {
      return new Traversal(this);
    }

    /**
     * "Flatten" the entire path into a string of single objects
     * by converting every node and edge into that object.
     *
     * @param nodeFunction converting function for nodes
     * @param edgeFunction converting function for edges
     * @param <X>          the type of object to convert into
     * @return the list of new typed objects
     */
    public <X> List<X> flatten(Function<Node, X> nodeFunction, Function<Edge, X> edgeFunction) {
      List<X> flattened = new LinkedList<>();
      for (int i = 0; i < edges.size(); i++) {
        flattened.add(nodeFunction.apply(nodes.get(i)));
        flattened.add(edgeFunction.apply(edges.get(i)));
      }
      flattened.add(nodeFunction.apply(nodes.get(nodes.size() - 1)));
      return flattened;
    }

  }

  public final Path findMinimumPath(Node origin, Node destination) {
    PriorityQueue<Node> toVisit = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));
    Set<Node> visited = new HashSet<>();

    origin.setDistance(0);
    origin.setPrevious(null);
    toVisit.add(origin);

    Node current;
    while (!toVisit.isEmpty()) {
      current = toVisit.poll();
      visited.add(current);

      if (current.equals(destination)) {
        // We've reached destination. Package solution.
        Path.Builder pathBuilder = Path.builder(destination);
        while (!current.equals(origin)) {
          pathBuilder.addEdge(this.edges.get(current.getPrevious(), current), current.getPrevious());
          current = current.getPrevious();
        }
        return pathBuilder.build();
      } else {
        // Not yet done
        for (Map.Entry<Node, Edge> outlet : edges.row(current).entrySet()) {
          // outlet.getKey() is destination
          // outlet.getValue() is edge from 'current' to destination
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
    }

    return null;  // Could not find it

  }

}
