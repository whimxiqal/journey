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
import com.google.common.collect.Table;
import edu.whimc.indicator.common.tools.AlternatingList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class WeightedGraph<N, E> {

  private final Set<Node> nodes = new HashSet<>();
  private final Table<Node, Node, E> edges = HashBasedTable.create();

  public void addEdge(@NotNull Node origin, @NotNull Node destination, @NotNull E edge) {
    this.nodes.add(origin);
    this.nodes.add(destination);
    this.edges.put(origin, destination, edge);
  }

  @Nullable
  protected final AlternatingList<Node, E, Object> findMinimumPath(Node origin, Node destination) {

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
        AlternatingList.Builder<Node, E, Object> pathBuilder = AlternatingList.builder(destination);
        while (!current.equals(origin)) {
          pathBuilder.addFirst(current.getPrevious(), this.edges.get(current.getPrevious(), current));
          current = current.getPrevious();
        }

        resetNodes();
        return pathBuilder.build();
      } else {
        // Not yet done
        for (Map.Entry<Node, E> outlet : edges.row(current).entrySet()) {
          // outlet.getKey() is destination
          // outlet.getValue() is edge from 'current' to destination
          if (visited.contains(outlet.getKey())) {
            continue;
          }
          if (outlet.getKey().getDistance() > current.getDistance() + edgeLength(outlet.getValue())) {
            // A better path for this node would be to come from current.
            // We can assume that is already queued. Remove from waiting queue to update.
            toVisit.remove(outlet.getKey());
            outlet.getKey().setDistance(current.getDistance() + edgeLength(outlet.getValue()) + nodeWeight(outlet.getKey().getData()));
            outlet.getKey().setPrevious(current);
            toVisit.add(outlet.getKey());
          }
        }
      }
    }

    resetNodes();
    return null;  // Could not find it

  }

  private void resetNodes() {
    this.nodes.forEach(node -> {
      node.setDistance(Double.MAX_VALUE);
      node.setPrevious(null);
    });
  }

  protected abstract double nodeWeight(N nodeData);

  protected abstract double edgeLength(E edge);

  public class Node {

    private final N data;
    private double distance = Double.MAX_VALUE;
    private Node previous = null;

    public Node(N data) {
      this.data = data;
    }

    public N getData() {
      return data;
    }

    public void setDistance(double distance) {
      this.distance = distance;
    }

    public double getDistance() {
      return distance;
    }

    public void setPrevious(Node previous) {
      this.previous = previous;
    }

    public Node getPrevious() {
      return previous;
    }

    @Override
    public String toString() {
      return String.format("Node: {data: %s, distance: %s, weight: %f}", data.hashCode(), distance > Double.MAX_VALUE * .9 ? "inf" : distance, nodeWeight(data));
    }
  }

}
