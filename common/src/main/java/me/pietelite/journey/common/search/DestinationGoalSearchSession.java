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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.message.Formatter;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.Itinerary;
import me.pietelite.journey.common.navigation.Port;
import me.pietelite.journey.common.search.event.FoundSolutionEvent;
import me.pietelite.journey.common.search.event.IgnoreCacheSearchEvent;
import me.pietelite.journey.common.search.event.StartSearchEvent;
import me.pietelite.journey.common.search.event.StopSearchEvent;
import me.pietelite.journey.common.search.flag.FlagSet;

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
    switch (state.get()) {
      case IDLE:
        initSearch();
        runSearchUnit();
        break;
      case RUNNING:
      case RUNNING_SUCCESSFUL:
        runSearchUnit();
        break;
      default:
        state.getAndUpdate(ResultState::stoppedResult);
        break;
    }
  }

  private void initSearch() {
    state.set(ResultState.RUNNING);
    stateInfo = new State();
    stateInfo.startTime = System.currentTimeMillis();
    Journey.get().dispatcher().dispatch(new StartSearchEvent(this));
    Journey.get().debugManager().broadcast(Formatter.debug("Started a search for caller ___, modes:___, ports:___",
            getCallerId(),
            modes().size(),
            ports().size()),
        getCallerId());

    state.set(ResultState.RUNNING);

    for (Port port : this.ports) {
      stateInfo.allDomains.add(port.getOrigin().domainId());
      stateInfo.allDomains.add(port.getDestination().domainId());
    }

    // Prepare port maps
    for (String domain : stateInfo.allDomains) {
      stateInfo.portsByOriginDomain.put(domain, new LinkedList<>());
      stateInfo.portsByDestinationDomain.put(domain, new LinkedList<>());
    }

    // Fill port maps
    for (Port port : this.ports) {
      stateInfo.portsByOriginDomain.get(port.getOrigin().domainId()).add(port);
      stateInfo.portsByDestinationDomain.get(port.getDestination().domainId()).add(port);
    }

    stateInfo.searchGraph = new SearchGraph(this, origin, destination);

    // Collect path trials
    if (origin.domainId().equals(destination.domainId())) {
      stateInfo.searchGraph.addPathTrialOriginToDestination(this.modes, persistentOrigin && persistentDestination);
    }

    for (String domain : stateInfo.allDomains) {
      for (Port pathTrialOriginPort : stateInfo.portsByDestinationDomain.get(domain)) {
        for (Port pathTrialDestinationPort : stateInfo.portsByOriginDomain.get(domain)) {
          stateInfo.searchGraph.addPathTrialPortToPort(
              pathTrialOriginPort,
              pathTrialDestinationPort,
              this.modes);
          if (domain.equals(origin.domainId())) {
            stateInfo.searchGraph.addPathTrialOriginToPort(pathTrialDestinationPort, this.modes, persistentOrigin);
          }
          if (domain.equals(destination.domainId())) {
            stateInfo.searchGraph.addPathTrialPortToDestination(pathTrialOriginPort, this.modes, persistentDestination);
          }
        }
      }
    }
  }

  private void runSearchUnit() {
    if (state.get().shouldStop()) {
      markStopped();
      return;
    }

    ItineraryTrial itineraryTrial = stateInfo.searchGraph.calculate();
    if (itineraryTrial == null) {
      // There is no possible solution to the entire problem
      state.set(ResultState.STOPPED_FAILED);
      Journey.get().dispatcher().dispatch(new StopSearchEvent(this));
      return;
    } else {
      // We have an overall solution with individual paths that aren't verified/calculated
      ItineraryTrial.TrialResult trialResult = itineraryTrial.attempt(stateInfo.usingCache);
      if (trialResult.itinerary().isPresent()) {
        // There is an itinerary solution!

        if (stateInfo.bestItinerary == null || trialResult.itinerary().get().cost() < stateInfo.bestItinerary.cost()) {
          stateInfo.bestItinerary = trialResult.itinerary().get();
          state.set(ResultState.RUNNING_SUCCESSFUL);
          Journey.get().dispatcher().dispatch(new FoundSolutionEvent(this, trialResult.itinerary().get()));
        }
      }

      // Do a quick check to see if the search was canceled before we try to return a value
      if (state.get().shouldStop()) {
        markStopped();
        return;
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

  private void markStopped() {
    state.getAndUpdate(ResultState::stoppedResult);
    Journey.get().dispatcher().dispatch(new StopSearchEvent(this));
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
    final Map<String, List<Port>> portsByOriginDomain = new HashMap<>();
    final Map<String, List<Port>> portsByDestinationDomain = new HashMap<>();
    long startTime;
    SearchGraph searchGraph = null;
    Itinerary bestItinerary = null;
    boolean usingCache = true;
  }

}
