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

package edu.whimc.indicator.common.search;

import edu.whimc.indicator.common.navigation.Cell;
import edu.whimc.indicator.common.navigation.Leap;
import edu.whimc.indicator.common.navigation.ModeType;
import edu.whimc.indicator.common.search.ItineraryTrial;
import edu.whimc.indicator.common.search.PathTrial;
import edu.whimc.indicator.common.search.graph.WeightedGraph;
import edu.whimc.indicator.common.tools.AlternatingList;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public final class SearchGraph<T extends Cell<T, D>, D> extends WeightedGraph {

  private final SearchSession<T, D> session;
  private final T origin;
  private final Node originNode;
  private final T destination;
  private final Node destinationNode;
  private final Map<Node, Leap<T, D>> nodeToLeap = new HashMap<>();
  private final Map<Edge, PathTrial<T, D>> edgeToPathTrial = new HashMap<>();

  private final Map<Leap<T, D>, Node> leapToNode = new HashMap<>();
  private final Map<PathTrial<T, D>, Edge> pathTrialToEdge = new HashMap<>();

  public SearchGraph(SearchSession<T, D> session, T origin, T destination, Collection<Leap<T, D>> leaps) {
    this.session = session;
    this.origin = origin;
    this.originNode = new Node(0);
    nodeToLeap.put(this.originNode, new Leap<>(this.origin, this.origin, ModeType.NONE, 0));
    this.destination = destination;
    this.destinationNode = new Node(0);
    nodeToLeap.put(this.destinationNode, new Leap<>(this.destination, this.destination, ModeType.NONE, 0));

    leaps.forEach(leap -> {
      Node leapNode = new Node(leap.getLength());
      nodeToLeap.put(leapNode, leap);
      leapToNode.put(leap, leapNode);
    });
  }

  private Node getOriginNode() {
    return originNode;
  }

  private Node getDestinationNode() {
    return destinationNode;
  }

  private Node getLeapNode(Leap<T, D> leap) {
    return leapToNode.get(leap);
  }

  private void addPathTrial(PathTrial<T, D> trial, Node start, Node end) {
    Edge edge = new Edge(trial.getLength());
    addEdge(start, end, edge);
    this.edgeToPathTrial.put(edge, trial);
    this.pathTrialToEdge.put(trial, edge);
  }

  public void addPathTrialOriginToDestination() {
    addPathTrial(PathTrial.approximate(session, origin, destination), getOriginNode(), getDestinationNode());
  }

  public void addPathTrialOriginToLeap(Leap<T, D> end) {
    addPathTrial(PathTrial.approximate(session, origin, end.getOrigin()), getOriginNode(), getLeapNode(end));
  }

  public void addPathTrialLeapToDestination(Leap<T, D> start) {
    addPathTrial(PathTrial.approximate(session, start.getDestination(), destination),
        getLeapNode(start), getDestinationNode());
  }

  public void addPathTrialLeapToLeap(Leap<T, D> start, Leap<T, D> end) {
    addPathTrial(PathTrial.approximate(session, start.getDestination(), end.getOrigin()),
        getLeapNode(start), getLeapNode(end));
  }

  @Nullable
  public ItineraryTrial<T, D> calculate() {
    AlternatingList<Node, Edge, Object> graphPath = findMinimumPath(originNode, destinationNode);
    if (graphPath == null) {
      return null;
    } else {
      return new ItineraryTrial<>(origin,
          graphPath.convert(node -> Objects.requireNonNull(nodeToLeap.get(node)),
              edge -> Objects.requireNonNull(edgeToPathTrial.get(edge))));
    }
  }

}
