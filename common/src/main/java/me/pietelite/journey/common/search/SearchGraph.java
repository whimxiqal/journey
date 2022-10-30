/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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

package me.pietelite.journey.common.search;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.data.DataAccessException;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.Mode;
import me.pietelite.journey.common.navigation.ModeType;
import me.pietelite.journey.common.navigation.Port;
import me.pietelite.journey.common.search.graph.WeightedGraph;
import me.pietelite.journey.common.tools.AlternatingList;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation of a weighted graph to be used for the overall search algorithm.
 */
public final class SearchGraph extends WeightedGraph<Port, PathTrial> {

  private final SearchSession session;
  private final Cell origin;
  private final Node originNode;
  private final Cell destination;
  private final Node destinationNode;
  private final Map<Port, Node> portToNode = new HashMap<>();

  /**
   * General constructor.
   *
   * @param session     the search session
   * @param origin      the origin of the entire problem
   * @param destination the destination of the entire problem
   * @param ports       the ports
   */
  public SearchGraph(SearchSession session, Cell origin, Cell destination, Collection<Port> ports) {
    this.session = session;
    this.origin = origin;
    this.originNode = new Node(new Port(origin, origin, ModeType.NONE, 0));
    this.destination = destination;
    this.destinationNode = new Node(new Port(destination, destination, ModeType.NONE, 0));

    ports.forEach(port -> portToNode.put(port, new Node(port)));
  }

  private Node getOriginNode() {
    return originNode;
  }

  private Node getDestinationNode() {
    return destinationNode;
  }

  private Node getLeapNode(Port port) {
    return portToNode.get(port);
  }

  /**
   * Add a path trial to the search graph that supposedly goes
   * directly from the origin to the destination.
   *
   * @param modes the mode types to supposedly get from the origin to the destination
   */
  public void addPathTrialOriginToDestination(Collection<Mode> modes, boolean persistentEnds) {
    addPathTrial(session, origin, destination, getOriginNode(), getDestinationNode(), modes, persistentEnds);
  }

  /**
   * Add a path trial to the search graph that supposedly goes
   * from the origin of the entire search to a port.
   * The endpoint of the path, then, would be the origin of the port.
   *
   * @param end   the end of the path trial
   * @param modes the mode types used to traverse the path
   */
  public void addPathTrialOriginToPort(Port end, Collection<Mode> modes, boolean persistentOrigin) {
    addPathTrial(session,
        origin, end.getOrigin(),
        getOriginNode(), getLeapNode(end),
        modes, persistentOrigin);
  }

  /**
   * Add a path trial to the search graph that supposedly goes
   * from a port to the destination of the entire search
   * The origin of the path, then, would be the destination of the port.
   *
   * @param start the start of the path trial
   * @param modes the mode types used to traverse the path
   */
  public void addPathTrialPortToDestination(Port start, Collection<Mode> modes, boolean persistentDestination) {
    addPathTrial(session,
        start.getDestination(), destination,
        getLeapNode(start), getDestinationNode(),
        modes, persistentDestination);
  }

  /**
   * Add a path trial to the search graph that goes from a port to
   * another port.
   * The destination of the start port is the origin of this path trial,
   * and the origin of the end port is the destination of this path trial.
   *
   * @param start the starting port
   * @param end   the ending port
   * @param modes the mode types used to traverse the path
   */
  public void addPathTrialPortToPort(Port start, Port end, Collection<Mode> modes) {
    addPathTrial(session, start.getDestination(), end.getOrigin(),
        getLeapNode(start), getLeapNode(end), modes, true);
  }

  private void addPathTrial(SearchSession session, Cell origin, Cell destination,
                            WeightedGraph<Port, PathTrial>.Node originNode,
                            WeightedGraph<Port, PathTrial>.Node destinationNode,
                            Collection<Mode> modes, boolean saveOnComplete) {
    // First, try to access a cached path
    Set<ModeType> modeTypes = modes.stream().map(Mode::type).collect(Collectors.toSet());
    boolean added = false;
    try {
      if (Journey.get().dataManager()
          .pathRecordManager()
          .containsRecord(origin, destination, modeTypes)) {
        addPathTrial(PathTrial.cached(session, origin, destination,
                modes,
                Journey.get().dataManager()
                    .pathRecordManager()
                    .getPath(origin, destination, modeTypes)),
            originNode, destinationNode);
        added = true;
      }
    } catch (DataAccessException e) {
      e.printStackTrace();
    }
    if (!added) {
      addPathTrial(PathTrial.approximate(session, origin, destination, modes, saveOnComplete), originNode, destinationNode);
    }
  }

  private void addPathTrial(PathTrial trial, Node start, Node end) {
    addEdge(start, end, trial);
  }

  /**
   * Calculate an itinerary trial using this graph.
   * If none is found, then return null.
   *
   * @return the itinerary trial
   */
  @Nullable
  public ItineraryTrial calculate() {
    AlternatingList<Node, PathTrial, Object> graphPath = findMinimumPath(originNode, destinationNode);
    if (graphPath == null) {
      return null;
    } else {
      return new ItineraryTrial(session, origin,
          graphPath.convert(node -> Objects.requireNonNull(node.getData()),
              edge -> edge));
    }
  }

  @Override
  protected double nodeWeight(Port nodeData) {
    return nodeData.getCost();
  }

  @Override
  protected double edgeLength(PathTrial edge) {
    return edge.getLength();
  }
}
