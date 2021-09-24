package edu.whimc.indicator.common.search.tracker;

import edu.whimc.indicator.common.navigation.*;
import edu.whimc.indicator.common.search.Search;

public interface SearchTracker<T extends Cell<T, D>, D> {

  enum Result {
    FAILURE,
    SUCCESS
  }

 void searchStarted(Search<T, D> search);

 void acceptResult(T cell, Result result, ModeType modeType);

 void foundNewOptimalPath(Itinerary itinerary);

 void startTrailSearch(T origin, T destination);

 void trailSearchVisitation(Step<T, D> step);

 void trailSearchStep(Step<T, D> step);

 void completeTrailSearch(T origin, T destination, double distance);

 void memoryCapacityReached(T origin, T destination);

 void searchStopped(Search<T, D> search);

}
