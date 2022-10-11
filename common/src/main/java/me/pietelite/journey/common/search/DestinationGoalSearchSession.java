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
  private long executionStartTime = -1;

  public DestinationGoalSearchSession(UUID callerId, Caller callerType, FlagSet flags, Cell origin, Cell destination) {
    super(callerId, callerType, flags);
    this.origin = origin;
    this.destination = destination;
  }

  @Override
  protected void doSearch() {
    executionStartTime = System.currentTimeMillis();
    Journey.get().dispatcher().dispatch(new StartSearchEvent(this));

    state.set(ResultState.RUNNING);

    Set<String> allDomains = new HashSet<>();

    for (Port port : this.ports) {
      allDomains.add(port.getOrigin().domainId());
      allDomains.add(port.getDestination().domainId());
    }

    Map<String, List<Port>> portsByOriginDomain = new HashMap<>();
    Map<String, List<Port>> portsByDestinationDomain = new HashMap<>();
    // Prepare port maps
    for (String domain : allDomains) {
      portsByOriginDomain.put(domain, new LinkedList<>());
      portsByDestinationDomain.put(domain, new LinkedList<>());
    }

    // Fill port maps
    for (Port port : this.ports) {
      portsByOriginDomain.get(port.getOrigin().domainId()).add(port);
      portsByDestinationDomain.get(port.getDestination().domainId()).add(port);
    }

    SearchGraph graph = new SearchGraph(this, origin, destination, this.ports);

    // Collect path trials
    if (origin.domainId().equals(destination.domainId())) {
      graph.addPathTrialOriginToDestination(this.modes);
    }

    for (String domain : allDomains) {
      for (Port pathTrialOriginPort : portsByDestinationDomain.get(domain)) {
        for (Port pathTrialDestinationPort : portsByOriginDomain.get(domain)) {
          graph.addPathTrialPortToPort(
              pathTrialOriginPort,
              pathTrialDestinationPort,
              this.modes);
          if (domain.equals(origin.domainId())) {
            graph.addPathTrialOriginToPort(pathTrialDestinationPort, this.modes);
          }
          if (domain.equals(destination.domainId())) {
            graph.addPathTrialPortToDestination(pathTrialOriginPort, this.modes);
          }
        }
      }
    }

    Itinerary bestItinerary = null;
    boolean usingCache = true;
    while (!this.state.get().isCanceled()) {

      ItineraryTrial itineraryTrial = graph.calculate();
      if (itineraryTrial == null) {
        // There is no possible solution to the entire problem
        state.set(ResultState.STOPPED_FAILED);
        Journey.get().dispatcher().dispatch(new StopSearchEvent(this));
        return;
      } else {
        // We have an overall solution with individual paths that aren't verified/calculated
        ItineraryTrial.TrialResult trialResult = itineraryTrial.attempt(usingCache);
        if (trialResult.itinerary().isPresent()) {
          // There is an itinerary solution!

          if (bestItinerary == null
              || trialResult.itinerary().get().getLength() < bestItinerary.getLength()) {
            bestItinerary = trialResult.itinerary().get();
            state.set(ResultState.RUNNING_SUCCESSFUL);
            Journey.get().dispatcher().dispatch(
                new FoundSolutionEvent(this, trialResult.itinerary().get()));
          }
        }

        // Do a quick check to see if the search was canceled before we try to return a value
        if (state.get().isCanceled()) {
          break;
        }

        if (!trialResult.changedProblem()) {
          // This result did not change the problem.
          // If run again, then, we would get the same solution to the graph.
          if (usingCache) {
            // Turn off the use of the cache. Maybe we can find better solution by re-solving some paths.
            Journey.get().dispatcher().dispatch(new IgnoreCacheSearchEvent(this));
            usingCache = false;
            // continue...
          } else {
            // The problem hasn't changed, and we aren't using the cache,
            // so no better solutions are possible.
            if (state.get().isSuccessful()) {
              state.set(ResultState.STOPPED_SUCCESSFUL);  // in case we had the running-successful state
            } else {
              state.set(ResultState.STOPPED_FAILED);
            }
            Journey.get().dispatcher().dispatch(new StopSearchEvent(this));
            return;
          }
        }
      }
    }

    // Canceling...
    state.getAndUpdate(current -> {
      if (current.isSuccessful()) {
        return ResultState.STOPPED_SUCCESSFUL;
      } else {
        return ResultState.STOPPED_CANCELED;
      }
    });
    Journey.get().dispatcher().dispatch(new StopSearchEvent(this));
  }

  @Override
  public long executionTime() {
    if (executionStartTime < 0) {
      return -1;
    }
    return System.currentTimeMillis() - executionStartTime;
  }

}
