package net.whimxiqal.journey.search;

import java.util.List;

public record SearchResultImpl(Status status, List<SearchStep> steps) implements SearchResult {

  @Override
  public Status status() {
    return status;
  }

  @Override
  public List<SearchStep> path() {
    return steps;
  }
}
