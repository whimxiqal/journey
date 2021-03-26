package edu.whimc.indicator.common.search;

import com.google.common.collect.Lists;
import edu.whimc.indicator.common.path.Locatable;

import java.util.Comparator;
import java.util.LinkedList;

public class TrailSearchRequestQueue<T extends Locatable<T, D>, D> {

  private final LinkedList<TrailSearchRequest<T, D>> requestQueue = Lists.newLinkedList();

  public boolean isEmpty() {
    return requestQueue.isEmpty();
  }

  public void add(TrailSearchRequest<T, D> request) {
    this.requestQueue.add(request);
  }

  public TrailSearchRequest<T, D> pop() {
    return this.requestQueue.pop();
  }

  public void sortByEstimatedLength() {
    requestQueue.sort(Comparator.comparingDouble(request ->
        request.getOrigin().distanceToSquared(request.getDestination())));
  }

}
