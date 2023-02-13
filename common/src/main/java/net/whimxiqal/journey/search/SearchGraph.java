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

package net.whimxiqal.journey.search;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.data.DataAccessException;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.navigation.Mode;
import net.whimxiqal.journey.navigation.ModeType;
import net.whimxiqal.journey.search.graph.WeightedGraph;
import net.whimxiqal.journey.tools.AlternatingList;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation of a weighted graph to be used for the overall search algorithm.
 */
public final class SearchGraph extends WeightedGraph<Tunnel, PathTrial> {

  private final DestinationGoalSearchSession session;
  private final Cell origin;
  private final Tunnel originNode;
  private final Cell destination;
  private final Tunnel destinationNode;

  /**
   * General constructor.
   *
   * @param session     the search session
   * @param origin      the origin of the entire problem
   * @param destination the destination of the entire problem
   */
  public SearchGraph(DestinationGoalSearchSession session, Cell origin, Cell destination) {
    this.session = session;
    this.origin = origin;
    this.originNode = Tunnel.builder(origin, origin).cost(0).build();
    this.destination = destination;
    this.destinationNode = Tunnel.builder(destination, destination).cost(0).build();
  }

  private Tunnel getOriginNode() {
    return originNode;
  }

  private Tunnel getDestinationNode() {
    return destinationNode;
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
   * from the origin of the entire search to a tunnel.
   * The endpoint of the path, then, would be the origin of the tunnel.
   *
   * @param end   the end of the path trial
   * @param modes the mode types used to traverse the path
   */
  public void addPathTrialOriginToTunnel(Tunnel end, Collection<Mode> modes, boolean persistentOrigin) {
    addPathTrial(session,
        origin, end.origin(),
        getOriginNode(), end,
        modes, persistentOrigin);
  }

  /**
   * Add a path trial to the search graph that supposedly goes
   * from a tunnel to the destination of the entire search
   * The origin of the path, then, would be the destination of the tunnel.
   *
   * @param start the start of the path trial
   * @param modes the mode types used to traverse the path
   */
  public void addPathTrialTunnelToDestination(Tunnel start, Collection<Mode> modes, boolean persistentDestination) {
    addPathTrial(session,
        start.destination(), destination,
        start, getDestinationNode(),
        modes, persistentDestination);
  }

  /**
   * Add a path trial to the search graph that goes from a tunnel to
   * another tunnel.
   * The destination of the start tunnel is the origin of this path trial,
   * and the origin of the end tunnel is the destination of this path trial.
   *
   * @param start the starting tunnel
   * @param end   the ending tunnel
   * @param modes the mode types used to traverse the path
   */
  public void addPathTrialPortToPort(Tunnel start, Tunnel end, Collection<Mode> modes) {
    addPathTrial(session, start.destination(), end.origin(),
        start, end, modes, true);
  }

  private void addPathTrial(SearchSession session, Cell origin, Cell destination,
                            Tunnel originNode,
                            Tunnel destinationNode,
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

  private void addPathTrial(PathTrial trial, Tunnel start, Tunnel end) {
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
    AlternatingList<Tunnel, PathTrial, Object> graphPath = findMinimumPath(originNode, destinationNode);
    if (graphPath == null) {
      return null;
    } else {
      return new ItineraryTrial(session, origin, graphPath);
    }
  }

  @Override
  protected double nodeWeight(Tunnel nodeData) {
    return nodeData.cost();
  }

  @Override
  protected double edgeLength(PathTrial edge) {
    return edge.getLength();
  }
}
