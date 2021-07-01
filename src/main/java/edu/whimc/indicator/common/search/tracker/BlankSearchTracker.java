package edu.whimc.indicator.common.search.tracker;

import edu.whimc.indicator.common.path.*;
import edu.whimc.indicator.common.search.TwoLevelBreadthFirstSearch;

public class BlankSearchTracker<T extends Cell<T, D>, D> implements SearchTracker<T, D> {
  @Override
  public void searchStarted(TwoLevelBreadthFirstSearch<T, D> search) { }

  @Override
  public void acceptResult(T cell, Result result, ModeType modeType) { }

  @Override
  public void foundNewOptimalPath(Path<T, D> path) { }

  @Override
  public void startTrailSearch(T origin, T destination) { }

  @Override
  public void trailSearchVisitation(Step<T, D> step) { }

  @Override
  public void trailSearchStep(Step<T, D> step) { }

  @Override
  public void completeTrailSearch(T origin, T destination, double distance) { }

  @Override
  public void memoryCapacityReached(T origin, T destination) { }

  @Override
  public void searchStopped(TwoLevelBreadthFirstSearch<T, D> search) { }
}
