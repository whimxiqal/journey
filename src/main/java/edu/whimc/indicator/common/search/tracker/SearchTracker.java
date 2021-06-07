package edu.whimc.indicator.common.search.tracker;

import edu.whimc.indicator.common.path.*;

public interface SearchTracker<T extends Cell<T, D>, D> {

  enum Result {
    FAILURE,
    SUCCESS
  }

  void acceptResult(T cell, Result result, ModeType modeType);

  void foundNewOptimalPath(Path<T, D> path, Completion<T, D> completion);

  void startTrailSearch(T origin, T destination);

  void trailSearchVisitation(Step<T, D> step);

  void trailSearchStep(Step<T, D> step);

  void finishTrailSearch(T origin, T destination, double distance);

  void memoryCapacityReached(T origin, T destination);

}
