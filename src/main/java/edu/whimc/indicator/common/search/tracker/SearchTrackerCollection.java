package edu.whimc.indicator.common.search.tracker;

import edu.whimc.indicator.common.navigation.Cell;
import edu.whimc.indicator.common.navigation.Itinerary;
import edu.whimc.indicator.common.navigation.ModeType;
import edu.whimc.indicator.common.navigation.Step;
import edu.whimc.indicator.common.search.Search;
import java.util.Collection;
import java.util.LinkedList;

public class SearchTrackerCollection<T extends Cell<T, D>, D> implements SearchTracker<T, D> {

  private final LinkedList<SearchTracker<T, D>> trackers = new LinkedList<>();

  public void addTracker(SearchTracker<T, D> tracker) {
    this.trackers.add(tracker);
  }

  public void addAllTrackers(Collection<SearchTracker<T, D>> trackers) {
    this.trackers.addAll(trackers);
  }

  @Override
  public void searchStarted(Search<T, D> search) {
    trackers.forEach(tracker -> tracker.searchStarted(search));
  }

  @Override
  public void acceptResult(T cell, Result result, ModeType modeType) {
    trackers.forEach(tracker -> tracker.acceptResult(cell, result, modeType));
  }

  @Override
  public void foundNewOptimalPath(Itinerary itinerary) {
    trackers.forEach(tracker -> tracker.foundNewOptimalPath(itinerary));
  }

  @Override
  public void startTrailSearch(T origin, T destination) {
    trackers.forEach(tracker -> tracker.startTrailSearch(origin, destination));
  }

  @Override
  public void trailSearchVisitation(Step<T, D> step) {
    trackers.forEach(tracker -> tracker.trailSearchVisitation(step));
  }

  @Override
  public void trailSearchStep(Step<T, D> step) {
    trackers.forEach(tracker -> tracker.trailSearchStep(step));
  }

  @Override
  public void completeTrailSearch(T origin, T destination, double distance) {
    trackers.forEach(tracker -> tracker.completeTrailSearch(origin, destination, distance));
  }

  @Override
  public void memoryCapacityReached(T origin, T destination) {
    trackers.forEach(tracker -> tracker.memoryCapacityReached(origin, destination));
  }

  @Override
  public void searchStopped(Search<T, D> search) {
    trackers.forEach(tracker -> tracker.searchStopped(search));
  }
}
