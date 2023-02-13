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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.message.Formatter;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.search.event.FoundSolutionEvent;
import net.whimxiqal.journey.search.event.IgnoreCacheSearchEvent;
import net.whimxiqal.journey.search.event.StartSearchEvent;
import net.whimxiqal.journey.search.event.StopSearchEvent;
import net.whimxiqal.journey.search.flag.FlagSet;

/**
 * An implementation of the {@link SearchSession} that uses a "reverse"
 * and predictive method to calculated ideal Itineraries.
 *
 * <p>First, all possible paths will be collected into a series of {@link PathTrial}s.
 */
public abstract class DestinationGoalSearchSession extends SearchSession {

  private final Cell origin;
  private final Cell destination;
  private final boolean persistentOrigin;
  private final boolean persistentDestination;
  private State stateInfo = null;

  public DestinationGoalSearchSession(UUID callerId, Caller callerType, FlagSet flags,
                                      Cell origin, Cell destination,
                                      boolean persistentOrigin, boolean persistentDestination) {
    super(callerId, callerType, flags);
    this.origin = origin;
    this.destination = destination;
    this.persistentOrigin = persistentOrigin;
    this.persistentDestination = persistentDestination;
  }

  protected void resumeSearch() {
    boolean shouldInit;
    synchronized (this) {
      shouldInit = state == ResultState.IDLE;
    }
    if (shouldInit) {
      initSearch();
    }
    runSearchUnit();
  }

  private void initSearch() {
    synchronized (this) {
      if (state != ResultState.IDLE) {
        return;
      }
      state = ResultState.RUNNING;
    }
    stateInfo = new State();
    stateInfo.startTime = System.currentTimeMillis();
    Journey.get().dispatcher().dispatch(new StartSearchEvent(this));
    Journey.get().debugManager().broadcast(Formatter.debug("Started a search for caller ___, modes:___, tunnels:___",
            getCallerId(),
            modes().size(),
            tunnels().size()),
        getCallerId());

    for (Tunnel tunnel : this.tunnels) {
      stateInfo.allDomains.add(tunnel.origin().domainId());
      stateInfo.allDomains.add(tunnel.destination().domainId());
    }

    // Prepare tunnel maps
    for (String domain : stateInfo.allDomains) {
      stateInfo.tunnelsByOriginDomain.put(domain, new LinkedList<>());
      stateInfo.tunnelsByDestinationDomain.put(domain, new LinkedList<>());
    }

    // Fill tunnel maps
    for (Tunnel tunnel : this.tunnels) {
      stateInfo.tunnelsByOriginDomain.get(tunnel.origin().domainId()).add(tunnel);
      stateInfo.tunnelsByDestinationDomain.get(tunnel.destination().domainId()).add(tunnel);
    }

    stateInfo.searchGraph = new SearchGraph(this, origin, destination);

    // Collect path trials
    if (origin.domainId().equals(destination.domainId())) {
      stateInfo.searchGraph.addPathTrialOriginToDestination(this.modes, persistentOrigin && persistentDestination);
    }

    for (String domain : stateInfo.allDomains) {
      for (Tunnel pathTrialOriginTunnel : stateInfo.tunnelsByDestinationDomain.get(domain)) {
        for (Tunnel pathTrialDestinationTunnel : stateInfo.tunnelsByOriginDomain.get(domain)) {
          stateInfo.searchGraph.addPathTrialPortToPort(
              pathTrialOriginTunnel,
              pathTrialDestinationTunnel,
              this.modes);
          if (domain.equals(origin.domainId())) {
            stateInfo.searchGraph.addPathTrialOriginToTunnel(pathTrialDestinationTunnel, this.modes, persistentOrigin);
          }
          if (domain.equals(destination.domainId())) {
            stateInfo.searchGraph.addPathTrialTunnelToDestination(pathTrialOriginTunnel, this.modes, persistentDestination);
          }
        }
      }
    }
  }

  private void runSearchUnit() {
    synchronized (this) {
      if (state.shouldStop()) {
        markStopped();
        return;
      }
    }

    // Create itinerary
    ItineraryTrial itineraryTrial = stateInfo.searchGraph.calculate();

    synchronized (this) {
      if (state.shouldStop() || itineraryTrial == null) {
        // even if itinerary trial exists, we were too late :(
        markStopped();
        return;
      }
    }

    // Solve itinerary (We have an overall solution with individual paths that aren't verified/calculated)
    ItineraryTrial.TrialResult trialResult = itineraryTrial.attempt(stateInfo.usingCache);

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
        if (stateInfo.usingCache) {
          // Turn off the use of the cache. Maybe we can find better solution by re-solving some paths.
          Journey.get().dispatcher().dispatch(new IgnoreCacheSearchEvent(this));
          stateInfo.usingCache = false;
          // continue...
        } else {
          // The problem hasn't changed, and we aren't using the cache,
          // so no better solutions are possible.
          markStopped();
        }
      }
    }
  }

  /**
   * Set state as stopped and send dispatch for stopped event. This method should be idempotent.
   */
  public synchronized void markStopped() {
    ResultState previousState = state;
    state = state.stoppedResult();
    if (state != previousState) {
      Journey.get().dispatcher().dispatch(new StopSearchEvent(this));
    }
  }

  @Override
  public long executionTime() {
    if (stateInfo == null) {
      return -1;
    }
    return System.currentTimeMillis() - stateInfo.startTime;
  }

  private static class State {
    final Set<String> allDomains = new HashSet<>();
    final Map<String, List<Tunnel>> tunnelsByOriginDomain = new HashMap<>();
    final Map<String, List<Tunnel>> tunnelsByDestinationDomain = new HashMap<>();
    long startTime;
    SearchGraph searchGraph = null;
    Itinerary bestItinerary = null;
    boolean usingCache = true;
  }

}
