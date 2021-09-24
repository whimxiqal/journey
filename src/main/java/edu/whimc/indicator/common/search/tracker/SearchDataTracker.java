package edu.whimc.indicator.common.search.tracker;

import edu.whimc.indicator.common.data.DataManager;
import edu.whimc.indicator.common.navigation.*;
import edu.whimc.indicator.common.search.Search;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class SearchDataTracker<T extends Cell<T, D>, D> implements SearchTracker<T, D> {

  private final DataManager<T, D> dataManager;

  @Override
  public void searchStarted(Search<T, D> search) {
    // TODO implement
  }

  @Override
  public final void acceptResult(T cell, Result result, ModeType modeType) {
    // TODO do something with the data using the data manager
  }

  @Override
  public void foundNewOptimalPath(Itinerary itinerary) {
    // TODO implement
  }

  @Override
  public void startTrailSearch(T origin, T destination) {
    // TODO implement
  }

  @Override
  public void trailSearchVisitation(Step<T, D> step) {
    // TODO implement
  }

  @Override
  public void trailSearchStep(Step<T, D> step) {
    // TODO implement
  }

  @Override
  public void completeTrailSearch(T origin, T destination, double distance) {
    // TODO implement
  }

  @Override
  public void memoryCapacityReached(T origin, T destination) {
    // TODO implement
  }

  @Override
  public void searchStopped(Search<T, D> search) {
    // TODO implement
  }
}
