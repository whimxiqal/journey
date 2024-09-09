/*
 * MIT License
 *
 * Copyright (c) whimxiqal
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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.TargetFunction;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.navigation.Mode;
import net.whimxiqal.journey.tools.AlternatingList;
import org.jetbrains.annotations.Nullable;

public class DestinationSearchGraph extends SearchGraph {

  private final TargetFunction targetFunction;
  private final Predicate<Cell> satisfactionPredicate;
  private final Set<Tunnel> destinationNodes;

  public DestinationSearchGraph(GraphGoalSearchSession<DestinationSearchGraph> session, Cell origin, TargetFunction targetFunction, Predicate<Cell> satisfactionPredicate) {
    super(session, origin);
    this.targetFunction = targetFunction;
    this.satisfactionPredicate = satisfactionPredicate;
    this.destinationNodes = new HashSet<>();
  }

  private static Tunnel identity(Cell cell) {
    return Tunnel.builder(cell, cell).cost(0).build();
  }

  /**
   * Add a path trial to the search graph that supposedly goes
   * directly from the origin to the destination.
   *
   * @param modes the mode types to supposedly get from the origin to the destination
   */
  public void addPathTrialOriginToDestination(Collection<Mode> modes, boolean persistentEnds) {
    Cell destination = targetFunction.target(origin);
    if (destination == null || destination.domain() != origin.domain()) {
      return;
    }
    Tunnel destinationNode = identity(destination);
    destinationNodes.add(destinationNode);
    addPathTrial(session, origin, destination,
        originNode, destinationNode,
        satisfactionPredicate, modes, persistentEnds);
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
    Cell destination = targetFunction.target(origin);
    if (destination == null || destination.domain() != origin.domain()) {
      return;
    }
    Tunnel destinationNode = identity(destination);
    destinationNodes.add(destinationNode);
    addPathTrial(session,
        start.exit(), destination,
        start, destinationNode,
        satisfactionPredicate, modes, persistentDestination);
  }

  /**
   * Calculate an itinerary trial using this graph.
   * If none is found, then return null.
   *
   * @return the itinerary trial
   */
  @Nullable
  @Override
  public ItineraryTrial calculate(boolean mustUseCache) {
    AlternatingList<Tunnel, DestinationPathTrial, Object> graphPath = findMinimumPath(
        originNode, destinationNodes::contains,
        trial -> !mustUseCache || trial.isFromCache());
    if (graphPath == null) {
      return null;
    } else {
      return new ItineraryTrial(session, origin, graphPath, session.flags);
    }
  }
}
