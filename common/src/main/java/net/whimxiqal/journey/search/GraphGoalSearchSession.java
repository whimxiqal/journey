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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.message.Formatter;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.search.event.FoundSolutionEvent;
import net.whimxiqal.journey.search.event.IgnoreCacheSearchEvent;
import net.whimxiqal.journey.search.event.StartSearchEvent;

/**
 * An implementation of the {@link SearchSession} that uses a "reverse"
 * and predictive method to calculated ideal Itineraries.
 *
 * <p>First, all possible paths will be collected into a series of {@link PathTrial}s.
 */
public abstract class GraphGoalSearchSession<G extends SearchGraph> extends SearchSession {

  protected final Cell origin;
  protected final boolean persistentOrigin;
  protected State stateInfo = null;

  enum CachingStatus {
    ALWAYS_USE,
    USE_IF_POSSIBLE,
    NEVER_USE,
  }

  public GraphGoalSearchSession(UUID callerId, Caller callerType,
                                Cell origin, boolean persistentOrigin) {
    super(callerId, callerType);
    this.origin = origin;
    this.persistentOrigin = persistentOrigin;
  }

  @Override
  protected void resumeSearch() {
    boolean shouldInit = false;
    synchronized (this) {
      if (state == ResultState.IDLE) {
        shouldInit = true;
        state = ResultState.RUNNING;
      }
    }
    if (shouldInit) {
      initSearch();
    }
    runSearchUnit();
  }

  private void initSearch() {
    stateInfo = new State();
    super.timer.start();
    Journey.get().dispatcher().dispatch(new StartSearchEvent(this));
    Journey.get().debugManager().broadcast(Formatter.debug("Started a search for caller ___, modes:___, tunnels:___",
            getCallerId(),
            modes().size(),
            tunnels().size()),
        getCallerId());

    for (Tunnel tunnel : this.tunnels) {
      stateInfo.allDomains.add(tunnel.origin().domain());
      stateInfo.allDomains.add(tunnel.destination().domain());
    }

    // Prepare tunnel maps
    for (Integer domain : stateInfo.allDomains) {
      stateInfo.tunnelsByOriginDomain.put(domain, new LinkedList<>());
      stateInfo.tunnelsByDestinationDomain.put(domain, new LinkedList<>());
    }

    // Fill tunnel maps
    for (Tunnel tunnel : this.tunnels) {
      stateInfo.tunnelsByOriginDomain.get(tunnel.origin().domain()).add(tunnel);
      stateInfo.tunnelsByDestinationDomain.get(tunnel.destination().domain()).add(tunnel);
    }

    stateInfo.searchGraph = createSearchGraph();

    // Collect path trials
    for (Integer domain : stateInfo.allDomains) {
      for (Tunnel pathTrialOriginTunnel : stateInfo.tunnelsByDestinationDomain.get(domain)) {
        for (Tunnel pathTrialDestinationTunnel : stateInfo.tunnelsByOriginDomain.get(domain)) {
          stateInfo.searchGraph.addPathTrialTunnelToTunnel(
              pathTrialOriginTunnel,
              pathTrialDestinationTunnel,
              this.modes);
          if (domain.equals(origin.domain())) {
            stateInfo.searchGraph.addPathTrialOriginToTunnel(pathTrialDestinationTunnel, this.modes, persistentOrigin);
          }
        }
      }
    }

    initSearchExtra();
  }

  abstract G createSearchGraph();

  protected void initSearchExtra() {
    // do nothing by default
  }

  private void runSearchUnit() {
    synchronized (this) {
      if (state.shouldStop()) {
        markStopped();
        return;
      }
    }

    // Create itinerary
    ItineraryTrial itineraryTrial = stateInfo.searchGraph.calculate(stateInfo.cachingStatus == CachingStatus.ALWAYS_USE);

    synchronized (this) {
      if (state.shouldStop() || itineraryTrial == null) {
        // even if itinerary trial exists, we were too late :(
        markStopped();
        return;
      }
    }

    // Solve itinerary (We have an overall solution with individual paths that aren't verified/calculated)
    ItineraryTrial.TrialResult trialResult = itineraryTrial.attempt(stateInfo.cachingStatus != CachingStatus.NEVER_USE);

    synchronized (this) {
      if (trialResult.itinerary().isPresent()) {
        // There is an itinerary solution!
        if (stateInfo.bestItinerary == null || trialResult.itinerary().get().cost() < stateInfo.bestItinerary.cost()) {
          stateInfo.bestItinerary = trialResult.itinerary().get();
          state = ResultState.RUNNING_SUCCESSFUL;
          Journey.get().dispatcher().dispatch(new FoundSolutionEvent(this, trialResult.itinerary().get()));
        }
      }

      if (!trialResult.changedProblem()) {
        // This result did not change the problem.
        // If run again, then, we would get the same solution to the graph.
        if (stateInfo.cachingStatus == CachingStatus.ALWAYS_USE) {
          // Turn off the use of the cache. Maybe we can find better solution by re-solving some paths.
          stateInfo.cachingStatus = CachingStatus.USE_IF_POSSIBLE;
          // continue...
        } else if (stateInfo.cachingStatus == CachingStatus.USE_IF_POSSIBLE) {
          Journey.get().dispatcher().dispatch(new IgnoreCacheSearchEvent(this));
          stateInfo.cachingStatus = CachingStatus.NEVER_USE;
        } else {
          // The problem hasn't changed, and we aren't using the cache,
          // so no better solutions are possible.
          markStopped();
        }
      }
    }
  }

  protected class State {
    final Set<Integer> allDomains = new HashSet<>();
    final Map<Integer, List<Tunnel>> tunnelsByOriginDomain = new HashMap<>();
    final Map<Integer, List<Tunnel>> tunnelsByDestinationDomain = new HashMap<>();
    G searchGraph = null;
    Itinerary bestItinerary = null;
    GraphGoalSearchSession.CachingStatus cachingStatus = GraphGoalSearchSession.CachingStatus.ALWAYS_USE;
  }

}
