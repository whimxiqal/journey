package edu.whimc.indicator.common.search.tracker;

import edu.whimc.indicator.common.path.*;
import edu.whimc.indicator.common.search.TwoLevelBreadthFirstSearch;

public interface SearchTracker<T extends Cell<T, D>, D> {

  enum Result {
    FAILURE,
    SUCCESS
  }

 void searchStarted(TwoLevelBreadthFirstSearch<T, D> search);

 void acceptResult(T cell, Result result, ModeType modeType);

 void foundNewOptimalPath(Path<T, D> path);

 void startTrailSearch(T origin, T destination);

 void trailSearchVisitation(Step<T, D> step);

 void trailSearchStep(Step<T, D> step);

 void completeTrailSearch(T origin, T destination, double distance);

 void memoryCapacityReached(T origin, T destination);

 void searchStopped(TwoLevelBreadthFirstSearch<T, D> search);

}
