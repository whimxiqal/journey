package edu.whimc.indicator.common.search;

import com.google.common.collect.Lists;
import edu.whimc.indicator.common.path.Cell;
import edu.whimc.indicator.common.path.Mode;
import edu.whimc.indicator.common.path.Trail;
import edu.whimc.indicator.common.search.tracker.SearchTracker;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LocalSearchRequestQueue<T extends Cell<T, D>, D> {

  private final LinkedList<LocalSearchRequest<T, D>> originRequestQueue = Lists.newLinkedList();
  private final LinkedList<LocalSearchRequest<T, D>> destinationRequestQueue = Lists.newLinkedList();
  private final LinkedList<LocalSearchRequest<T, D>> linkRequestQueue = Lists.newLinkedList();

  private boolean foundOriginTrail = false;
  private boolean foundDestinationTrail = false;
  @Getter
  private boolean impossibleResult = false;
  private boolean startedPopping = false;

  public boolean isEmpty() {
    return originRequestQueue.isEmpty()
        && destinationRequestQueue.isEmpty()
        && linkRequestQueue.isEmpty();
  }

  public int size() {
    return originRequestQueue.size() + destinationRequestQueue.size() + linkRequestQueue.size();
  }

  /**
   * Add a request to find a trail coming from the overall origin.
   *
   * @param request the trail request
   */
  public void addOriginRequest(LocalSearchRequest<T, D> request) {
    if (startedPopping) throw new IllegalStateException("You may not add any more requests "
        + "after you have started popping off the queue");
    this.originRequestQueue.add(request);
  }

  /**
   * Add a request to find a trail going to the overall destination.
   *
   * @param request the trail request
   */
  public void addDestinationRequest(LocalSearchRequest<T, D> request) {
    if (startedPopping) throw new IllegalStateException("You may not add any more requests "
        + "after you have started popping off the queue");
    this.destinationRequestQueue.add(request);
  }

  /**
   * Add a request to find a trail going between two links.
   *
   * @param request the trail request
   */
  public void addLinkRequest(LocalSearchRequest<T, D> request) {
    if (startedPopping) throw new IllegalStateException("You may not add any more requests "
        + "after you have started popping off the queue");
    this.linkRequestQueue.add(request);
  }

  /**
   * Pop a trail request from this queue in a custom order of importance.
   * If there haven't yet been trail found from the overall origin,
   * it will attempt to find those first. Then, it will check if any
   * are going directly to the overall destination. If at any point,
   * there are no trails either going from the the overall origin or
   * to the overall destination, fail.
   *
   * @param localSearch      the searching environment
   * @param requestPredicate a predicate to superficially identify if a request is worth pursuing
   * @return the trail from the trail search, or null if the final result is impossible.
   */
  @Nullable
  public Trail<T, D> popAndRunIntelligentlyIf(LocalSearch<T, D> localSearch,
                                              Collection<Mode<T, D>> modes,
                                              Predicate<LocalSearchRequest<T, D>> requestPredicate,
                                              SearchTracker<T, D> tracker) {
    startedPopping = true;
    if (isEmpty()) throw new NoSuchElementException();
    LocalSearchRequest<T, D> request;

    boolean findingOrigin = false;
    boolean findingDestination = false;
    // finding Link can be inferred form other boolean terms
    if (!foundOriginTrail) {
      if (originRequestQueue.isEmpty()) {
        impossibleResult = true;
        return null;
      } else {
        request = originRequestQueue.pop();
        findingOrigin = true;
      }
    } else if (!foundDestinationTrail) {
      if (destinationRequestQueue.isEmpty()) {
        impossibleResult = true;
        return null;
      } else {
        request = destinationRequestQueue.pop();
        findingDestination = true;
      }
    } else {
      double minimumLength = Double.MAX_VALUE;
      if (!originRequestQueue.isEmpty() && getApproximateLength(originRequestQueue.getFirst()) < minimumLength) {
        findingOrigin = true;
        minimumLength = getApproximateLength(originRequestQueue.getFirst());
      }
      if (!destinationRequestQueue.isEmpty() && getApproximateLength(destinationRequestQueue.getFirst()) < minimumLength) {
        findingOrigin = false;
        findingDestination = true;
        minimumLength = getApproximateLength(destinationRequestQueue.getFirst());
      }
      if (!linkRequestQueue.isEmpty() && getApproximateLength(linkRequestQueue.getFirst()) < minimumLength) {
        findingOrigin = false;
        findingDestination = false;
        // Don't set minimum length because we don't need to know it anymore
      }
      if (findingOrigin) {
        request = originRequestQueue.pop();
      } else if (findingDestination) {
        request = destinationRequestQueue.pop();
      } else {
        request = linkRequestQueue.pop();
      }
    }

    // Run test in case this request is not worth running
    if (!requestPredicate.test(request)) {
      return null;
    }

    Trail<T, D> trail = localSearch.findOptimalTrail(request, modes, tracker);

    // Mark flags if valid trail was found
    if (Trail.isValid(trail)) {
      if (findingOrigin) {
        foundOriginTrail = true;
      } else if (findingDestination) {
        foundDestinationTrail = true;
      }
    }

    return trail;
  }

  @Nullable
  public Trail<T, D> popAndRunLinkRequest(LocalSearch<T, D> localSearch,
                                          Collection<Mode<T, D>> modes,
                                          Consumer<LocalSearchRequest<T, D>> requestConsumer,
                                          SearchTracker<T, D> tracker) {
    startedPopping = true;
    if (linkRequestQueue.isEmpty()) throw new NoSuchElementException();
    LocalSearchRequest<T, D> request = linkRequestQueue.pop();
    requestConsumer.accept(request);
    return localSearch.findOptimalTrail(request, modes, tracker);
  }

  public void sortByEstimatedLength() {
    Comparator<LocalSearchRequest<T, D>> comparator = Comparator.comparingDouble(this::getApproximateLength);
    originRequestQueue.sort(comparator);
    destinationRequestQueue.sort(comparator);
    linkRequestQueue.sort(comparator);
  }

  private double getApproximateLength(LocalSearchRequest<T, D> request) {
    return request.getOrigin().distanceToSquared(request.getDestination());
  }

}
