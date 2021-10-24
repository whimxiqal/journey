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

package edu.whimc.journey.common.search;

import edu.whimc.journey.common.JourneyCommon;
import edu.whimc.journey.common.navigation.Cell;
import edu.whimc.journey.common.navigation.Itinerary;
import edu.whimc.journey.common.navigation.Leap;
import edu.whimc.journey.common.navigation.ModeTypeGroup;
import edu.whimc.journey.common.search.event.FoundSolutionEvent;
import edu.whimc.journey.common.search.event.StartSearchEvent;
import edu.whimc.journey.common.search.event.StopSearchEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * An implementation of the {@link SearchSession} that uses a "reverse"
 * and predictive method to calculated ideal Itineraries.
 *
 * <p>First, all possible paths will be collected into a series of {@link PathTrial}s.
 *
 *
 * @param <T> the cell type
 * @param <D> the domain type
 */
public abstract class ReverseSearchSession<T extends Cell<T, D>, D> extends SearchSession<T, D> {

  public ReverseSearchSession(UUID callerId, Caller callerType) {
    super(callerId, callerType);
  }

  @Override
  public final void search(T origin, T destination) {

    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StartSearchEvent<>(this));

    state = ResultState.RUNNING;

    Set<D> allDomains = new HashSet<>();

    for (Leap<T, D> leap : this.leaps) {
      allDomains.add(leap.getOrigin().getDomain());
      allDomains.add(leap.getDestination().getDomain());
    }

    Map<D, List<Leap<T, D>>> leapsByOriginDomain = new HashMap<>();
    Map<D, List<Leap<T, D>>> leapsByDestinationDomain = new HashMap<>();
    // Prepare leap maps
    for (D domain : allDomains) {
      leapsByOriginDomain.put(domain, new LinkedList<>());
      leapsByDestinationDomain.put(domain, new LinkedList<>());
    }

    // Fill leap maps
    for (Leap<T, D> leap : this.leaps) {
      leapsByOriginDomain.get(leap.getOrigin().getDomain()).add(leap);
      leapsByDestinationDomain.get(leap.getDestination().getDomain()).add(leap);
    }

    SearchGraph<T, D> graph = new SearchGraph<>(this, origin, destination, this.leaps);

    // Collect path trials
    ModeTypeGroup modeTypeGroup = ModeTypeGroup.from(this.modes);
    if (origin.getDomain().equals(destination.getDomain())) {
      graph.addPathTrialOriginToDestination(modeTypeGroup);
    }

    for (D domain : allDomains) {
      for (Leap<T, D> pathTrialOriginLeap : leapsByDestinationDomain.get(domain)) {
        for (Leap<T, D> pathTrialDestinationLeap : leapsByOriginDomain.get(domain)) {
          graph.addPathTrialLeapToLeap(
              pathTrialOriginLeap,
              pathTrialDestinationLeap,
              modeTypeGroup);
          if (domain.equals(origin.getDomain())) {
            graph.addPathTrialOriginToLeap(pathTrialDestinationLeap, modeTypeGroup);
          }
          if (domain.equals(destination.getDomain())) {
            graph.addPathTrialLeapToDestination(pathTrialOriginLeap, modeTypeGroup);
          }
        }
      }
    }

    Itinerary<T, D> bestItinerary = null;
    boolean usingCache = true;
    boolean noSolution;
    while (!this.state.isCanceled()) {

      ItineraryTrial<T, D> itineraryTrial = graph.calculate();
      noSolution = false;
      if (itineraryTrial == null) {
        noSolution = true;
      } else {
        ItineraryTrial.TrialResult<T, D> trialResult = itineraryTrial.attempt(this.modes, usingCache);
        if (trialResult.itinerary().isPresent()) {
          if (!trialResult.changedProblem()) {
            // This result did not change the problem, as in,
            //  it did not affect the values that affect the algorithm, so we will get the
            //  same solution every time. So, stop solving.

            if (this.state.isRunning()) {
              // Another solution hasn't yet been found. That means this solution was found
              //  without changing a single piece of data so all trails must have been cached.
              //  That's fine, just set the state to successful and return this sole solution.
              this.state = ResultState.SUCCESSFUL;
              JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(
                  new FoundSolutionEvent<>(this, trialResult.itinerary().get()));
            }
            JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopSearchEvent<>(this));
            return;
          }
          if (bestItinerary == null
              || bestItinerary.getLength() > trialResult.itinerary().get().getLength()) {
            // TODO verify if all the paths still work (some of them might be cached)
            bestItinerary = trialResult.itinerary().get();
            this.state = ResultState.SUCCESSFUL;
            JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(
                new FoundSolutionEvent<>(this, trialResult.itinerary().get()));
          }
        } else {
          if (!trialResult.changedProblem()) {
            // We couldn't find any solution to the itinerary trial and nothing has changed,
            //  so there isn't any solution.
            noSolution = true;
          }
        }
      }

      if (noSolution) {
        if (usingCache) {
          // We've been using cache up to this point.
          //  Let's switch into not using the cache because it may have been wrong,
          //  even though it takes longer to calculate now.
          //  These solutions will probably not work!
          usingCache = false;
          continue;
        }
        // We couldn't find any solution to the graph, meaning no overall solution is possible
        state = ResultState.FAILED;
        JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopSearchEvent<>(this));
        return;
      }
    }

    // We are canceled
    if (!state.isSuccessful()) {
      state = ResultState.CANCELED;
    }
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopSearchEvent<>(this));
  }
}
