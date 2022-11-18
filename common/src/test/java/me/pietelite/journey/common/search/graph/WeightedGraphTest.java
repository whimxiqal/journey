package me.pietelite.journey.common.search.graph;

import java.util.Collection;
import java.util.Iterator;
import me.pietelite.journey.common.tools.AlternatingList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WeightedGraphTest {

  @Test
  void findMinimumPath() {
    Graph graph = new Graph();
    Node A = new Node(0);
    Node B = new Node(0);
    Node C = new Node(0);
    Node D = new Node(0);
    Node E = new Node(0);
    Node F = new Node(0);
    graph.addEdge(A, B, new Edge(9));
    graph.addEdge(A, C, new Edge(4));
    graph.addEdge(C, B, new Edge(4));
    graph.addEdge(B, D, new Edge(12));
    graph.addEdge(B, E, new Edge(5));
    graph.addEdge(C, E, new Edge(13));
    graph.addEdge(D, F, new Edge(2));
    graph.addEdge(E, D, new Edge(3));
    graph.addEdge(E, F, new Edge(15));
    AlternatingList<Node, Edge, Object> result = graph.findMinimumPath(A, F);
    Assertions.assertNotNull(result);

    Collection<Node> nodes = result.getMajors();
    Assertions.assertEquals(6, nodes.size());

    Iterator<Node> nodesIt = nodes.iterator();
    Assertions.assertEquals(A, nodesIt.next());
    Assertions.assertEquals(C, nodesIt.next());
    Assertions.assertEquals(B, nodesIt.next());
    Assertions.assertEquals(E, nodesIt.next());
    Assertions.assertEquals(D, nodesIt.next());
    Assertions.assertEquals(F, nodesIt.next());

    Collection<Edge> edges = result.getMinors();
    Assertions.assertEquals(5, edges.size());

    Iterator<Edge> edgesIt = edges.iterator();
    Assertions.assertEquals(4, edgesIt.next().length);
    Assertions.assertEquals(4, edgesIt.next().length);
    Assertions.assertEquals(5, edgesIt.next().length);
    Assertions.assertEquals(3, edgesIt.next().length);
    Assertions.assertEquals(2, edgesIt.next().length);
  }

  private class Graph extends WeightedGraph<Node, Edge> {

    @Override
    protected double nodeWeight(Node nodeData) {
      return nodeData.weight;
    }

    @Override
    protected double edgeLength(Edge edge) {
      return edge.length;
    }
  }

  private class Node {
    Node(double weight) {
      this.weight = weight;
    }
    private final double weight;
  }

  private class Edge {
    Edge(double length) {
      this.length = length;
    }
    private final double length;
  }
}