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

import edu.whimc.indicator.common.IndicatorCommon;
import edu.whimc.indicator.common.navigation.Cell;
import edu.whimc.indicator.common.navigation.Itinerary;
import edu.whimc.indicator.common.navigation.Leap;
import edu.whimc.indicator.common.search.event.FoundSolutionEvent;
import edu.whimc.indicator.common.search.event.SearchDispatcher;
import edu.whimc.indicator.common.search.event.StopSearchEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  public ReverseSearchSession(UUID playerUuid, Caller callerType) {
    super(playerUuid, callerType);
  }

  @Override
  public final void search(T origin, T destination) {
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

    Itinerary<T, D> bestItinerary = null;
    boolean usingCache = true;
    while (!this.state.isCanceled()) {

      // TODO try to get this graph out of the while loop so we don't run it every time.
      //  For some reason, I didn't do that originally and I can't remember why,
      //  I think something to do with how I update path trials throughout one iteration
      SearchGraph<T, D> graph = new SearchGraph<>(this, origin, destination, this.leaps);

      // Collect path trials
      if (origin.getDomain().equals(destination.getDomain())) {
        graph.addPathTrialOriginToDestination();
      }

      for (D domain : allDomains) {
        for (Leap<T, D> pathTrialOriginLeap : leapsByDestinationDomain.get(domain)) {
          for (Leap<T, D> pathTrialDestinationLeap : leapsByOriginDomain.get(domain)) {
            graph.addPathTrialLeapToLeap(
                pathTrialOriginLeap,
                pathTrialDestinationLeap);
            if (domain.equals(origin.getDomain())) {
              graph.addPathTrialOriginToLeap(pathTrialDestinationLeap);
            }
            if (domain.equals(destination.getDomain())) {
              graph.addPathTrialLeapToDestination(pathTrialOriginLeap);
            }
          }
        }
      }

      ItineraryTrial<T, D> itineraryTrial = graph.calculate();
      if (itineraryTrial == null) {
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
        IndicatorCommon.<T, D>getSearchEventDispatcher().dispatch(new StopSearchEvent<>(this));
        return;
      }

      Optional<Itinerary<T, D>> itineraryOptional = itineraryTrial.attempt(this, this.modes, usingCache);
      if (itineraryOptional.isPresent()) {
        if (bestItinerary != null && bestItinerary.equals(itineraryOptional.get())) {
          // We found the same solution again, which probably means this is the best solution,
          // so stop searching.
          IndicatorCommon.<T, D>getSearchEventDispatcher().dispatch(new StopSearchEvent<>(this));
          return;
        }
        if (bestItinerary == null || bestItinerary.getLength() < itineraryOptional.get().getLength()) {
          // TODO verify if all the paths still work (some of them might be cached)
          bestItinerary = itineraryOptional.get();
          this.state = ResultState.SUCCESSFUL;
          IndicatorCommon.<T, D>getSearchEventDispatcher().dispatch(new FoundSolutionEvent<>(this, itineraryOptional.get()));
        }
      }
    }

    // We are canceled
    state = ResultState.CANCELED;
    IndicatorCommon.<T, D>getSearchEventDispatcher().dispatch(new StopSearchEvent<>(this));
  }
}
