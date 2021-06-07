package edu.whimc.indicator.common.search.tracker;

import edu.whimc.indicator.common.path.*;

public final class BlankSearchTracker<T extends Cell<T, D>, D> implements SearchTracker<T, D> {

  @Override
  public void acceptResult(T cell, Result result, ModeType modeType) {
    // nothing
  }

  @Override
  public void foundNewOptimalPath(Path<T, D> path, Completion<T, D> completion) {
    // nothing
  }

  @Override
  public void startTrailSearch(T origin, T destination) {
    // nothing
  }

  @Override
  public void trailSearchVisitation(Step<T, D> step) {
    // nothing
  }

  @Override
  public void trailSearchStep(Step<T, D> step) {
    // nothing
  }

  @Override
  public void finishTrailSearch(T origin, T destination, double distance) {
    // nothing
  }

  @Override
  public void memoryCapacityReached(T origin, T destination) {
    // nothing
  }
}
