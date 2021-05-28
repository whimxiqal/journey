/*
 * Copyright 2021 Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package edu.whimc.indicator.common.search;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.cache.TrailCache;
import edu.whimc.indicator.common.path.*;
import edu.whimc.indicator.common.util.TriConsumer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TwoLevelBreadthFirstSearch<T extends Locatable<T, D>, D> {

  public enum RunningStatus {
    IDLE,
    RUNNING,
    CANCELLED,
    COMPLETED,
  }

  private final Collection<Link<T, D>> links = Lists.newLinkedList();
  private final Collection<Mode<T, D>> modes = Lists.newLinkedList();
  private final ModeTypeGroup modeTypes = new ModeTypeGroup();
  private final TrailCache<T, D> trailCache;

  @Getter
  private RunningStatus runningStatus = RunningStatus.IDLE;
  private boolean succeeded = false;

  // Callbacks
  @Setter
  private Consumer<Path<T, D>> foundNewOptimalPathEvent = path -> {
  };
  @Setter
  private BiConsumer<T, T> startTrailSearchCallback = (l1, l2) -> {
  };
  @Setter
  private Consumer<Step<T, D>> trialSearchVisitationCallback = loc -> {
  };
  @Setter
  private Consumer<Step<T, D>> trailSearchStepCallback = loc -> {
  };
  @Setter
  private TriConsumer<T, T, Double> finishTrailSearchCallback = (l1, l2, integer) -> {
  };
  @Setter
  private BiConsumer<T, T> memoryCapacityErrorCallback = (l1, l2) -> {
  };

  public TwoLevelBreadthFirstSearch(TrailCache<T, D> trailCache) {
    this.trailCache = trailCache;
  }

  public void registerLink(Link<T, D> link) {
    this.links.add(link);
  }

  public void registerMode(Mode<T, D> mode) {
    if (modeTypes.contains(mode.getType())) {
      Indicator.getInstance().getLogger().severe("The mode " + mode
          + " has the same mode type (" + mode.getType()
          + " as another registered mode. Ignoring...");
      return;
    }
    this.modes.add(mode);
    this.modeTypes.add(mode.getType());
  }

  /**
   * From a list of all domain links, choose just the links that could get
   * us to the destination from the origin.
   *
   * @param origin      the starting location
   * @param destination the ending location
   * @param links       all domain connections
   * @return just the domain connections that could be part of the best answer
   */
  private List<Link<T, D>> filterLinks(T origin, T destination, Collection<Link<T, D>> links) {
    DomainGraph<D> graph = new DomainGraph<>();
    links.forEach(link -> graph.addEdge(link.getOrigin().getDomain(), link.getDestination().getDomain()));
    Set<D> onPath = graph.domainsOnConnectingPath(origin.getDomain(), destination.getDomain());
    return links.stream()
        .filter(link -> onPath.contains(link.getOrigin().getDomain())
            && onPath.contains(link.getDestination().getDomain()))
        .collect(Collectors.toList());
  }

  private Trail<T, D> queueTrailRequestIfNotCached(@Nullable T origin, @Nullable T destination,
                                                   TrailSearchRequestQueue<T, D> queue,
                                                   TrailSearchRequest<T, D> request) {
    Trail<T, D> trail = trailCache.get(request.getOrigin(), request.getDestination(), modeTypes);
    boolean shouldQueue = false;

    if (trail == null) {

      // There is no cached trail for these inputs, so queue a search
      shouldQueue = true;

      // See if we can get a trail that was cached with a subgroup of these mode types
      //  so that we can give an answer to the overall graph sooner
      trail = trailCache.getAnyMatching(request.getOrigin(), request.getDestination(), modeTypes);

    }

    if (trail != null) {
      // There's a cached trail!
      if (!trail.getSteps().isEmpty()) {
        // It's not an invalid so we have to verify that it still works
        //  (no verification needed for invalids, we will verify that
        //  it is invalid using a scheduled task)

        Step<T, D> prev = trail.getSteps().get(0);
        Step<T, D> curr;
        boolean validStep = true;
        for (int i = 1; i < trail.getSteps().size(); i++) {
          curr = trail.getSteps().get(i);
          validStep = false;  // Doesn't work until proven it does
          for (Mode<T, D> mode : modes) {
            if (mode.getDestinations(prev.getLocatable()).containsKey(curr.getLocatable())) {
              // This step works, keep checking
              validStep = true;
              break;
            }
          }
          if (!validStep) {
            // We couldn't find a way to make this step work
            break;
          }
          prev = curr;
        }
        if (!validStep) {
          // This trail no longer works. Requeue.
          trail = null;
          shouldQueue = true;
        }
      }
    }

    if (shouldQueue) {
      if (request.getOrigin().equals(origin)) {
        queue.addOriginRequest(request);
      } else if (request.getDestination().equals(destination)) {
        queue.addDestinationRequest(request);
      } else {
        queue.addLinkRequest(request);
      }
    }
    return trail;

  }

  private void findOptimalPath(T origin, T destination, List<Link<T, D>> links) {
    // Step 1 - organize filtered links into entry and exit points in every domain
    Map<D, Set<Link<T, D>>> entryDomains = collectEntryDomains(links);
    Map<D, Set<Link<T, D>>> exitDomains = collectExitDomains(links);

    // Step 2 - Initialize local-domain search class
    TrailSearch<T, D> trailSearch = new TrailSearch<>();
    trailSearch.setStepCallback(trailSearchStepCallback);
    trailSearch.setVisitationCallback(trialSearchVisitationCallback);

    // Step 3 - Set up graph
    PathEdgeGraph<T, D> graph = new PathEdgeGraph<>();
    TrailSearchRequestQueue<T, D> queue = new TrailSearchRequestQueue<>();
    Trail<T, D> trail;
    // Nodes
    PathEdgeGraph.Node originNode = graph.generateNode();
    PathEdgeGraph.Node destinationNode = graph.generateNode();

    Map<Link<T, D>, PathEdgeGraph.Node> linkNodeMap = new HashMap<>();
    links.forEach(link -> linkNodeMap.put(link, graph.generateLinkNode(link)));
    // Edges

    // Origin to Endpoint edge if they are in the same domain
    if (origin.getDomain().equals(destination.getDomain())) {
      trail = queueTrailRequestIfNotCached(origin, destination, queue,
          new TrailSearchRequest<>(origin, destination,
              originNode, destinationNode,
              this::isCancelled, false));
      if (trail != null) {
        graph.addEdge(originNode, destinationNode, trail);
      }
    }

    // QUEUE TRAILS
    // Queue trails: origin -> link
    if (exitDomains.containsKey(origin.getDomain())) {
      for (Link<T, D> exit : exitDomains.get(origin.getDomain())) {
        trail = queueTrailRequestIfNotCached(origin, destination, queue,
            new TrailSearchRequest<>(origin, exit.getOrigin(),
                originNode, linkNodeMap.computeIfAbsent(exit, graph::generateLinkNode),
                this::isCancelled, false));
        if (trail != null) {
          graph.addEdge(originNode, linkNodeMap.get(exit), trail);
        }
      }
    }
    // Queue trails: link -> destination
    if (entryDomains.containsKey(destination.getDomain())) {
      for (Link<T, D> entry : entryDomains.get(destination.getDomain())) {
        trail = queueTrailRequestIfNotCached(origin, destination, queue,
            new TrailSearchRequest<>(entry.getDestination(), destination,
                linkNodeMap.computeIfAbsent(entry, graph::generateLinkNode), destinationNode,
                this::isCancelled, false));
        if (trail != null) {
          graph.addEdge(linkNodeMap.get(entry), destinationNode, trail);
        }
      }
    }
    // Queue trails: link -> link
    Set<D> allDomains = new HashSet<>();
    allDomains.addAll(entryDomains.keySet());
    allDomains.addAll(exitDomains.keySet());

    for (D domain : allDomains) {
      if (entryDomains.containsKey(domain)) {
        for (Link<T, D> entry : entryDomains.get(domain)) {
          if (exitDomains.containsKey(domain)) {
            for (Link<T, D> exit : exitDomains.get(domain)) {
              if (entry.getOrigin().equals(exit.getDestination())) {
                continue;  // We don't want to use link <-> link if they just come back to the same spot
              }
              trail = queueTrailRequestIfNotCached(origin, destination, queue,
                  new TrailSearchRequest<>(entry.getDestination(), exit.getOrigin(),
                      linkNodeMap.computeIfAbsent(entry, graph::generateLinkNode),
                      linkNodeMap.computeIfAbsent(exit, graph::generateLinkNode),
                      this::isCancelled, true));
              if (trail != null) {
                graph.addEdge(linkNodeMap.get(entry), linkNodeMap.get(exit), trail);
              }
            }
          }
        }
      }
    }

    // Node mappings to make sure that we add to edges to the global graph correctly

    queue.sortByEstimatedLength();

    AtomicDouble optimalLength = new AtomicDouble(Math.sqrt(Double.MAX_VALUE) / 2);  // Very large
    AtomicReference<TrailSearchRequest<T, D>> trailSearchRequest = new AtomicReference<>();
    Trail<T, D> latestFoundTrail;
    Path<T, D> foundPath;
    while (!queue.isEmpty()) {

      // Start search from the queue
      latestFoundTrail = queue.popAndRunIntelligentlyIf(trailSearch, modes,
          request -> {
            trailSearchRequest.set(request);
            boolean willRun = request.getOrigin().distanceToSquared(request.getDestination()) < optimalLength.get() * optimalLength.get();
            if (willRun) {
              startTrailSearchCallback.accept(request.getOrigin(), request.getDestination());
            }
            return willRun;
          });
      if (queue.isImpossibleResult()) {
        // We have checked through some possibilities and found that no result is possible
        return;
      }
      if (latestFoundTrail == null) {
        continue;
      }
      finishTrailSearchCallback.accept(trailSearchRequest.get().getOrigin(), trailSearchRequest.get().getDestination(), latestFoundTrail.getLength());

      if (trailSearchRequest.get().isCacheable()) {
        trailCache.put(trailSearchRequest.get().getOrigin(), trailSearchRequest.get().getDestination(), modeTypes, latestFoundTrail);
      }

      if (Trail.isValid(latestFoundTrail)) {
        graph.addEdge(trailSearchRequest.get().getOriginNode(), trailSearchRequest.get().getDestinationNode(), latestFoundTrail);
      }

      // Step 5 - Solve graph - Find the minimum path from the domain graph
      foundPath = graph.findMinimumPath(originNode, destinationNode);
      if (foundPath != null && foundPath.getLength() < optimalLength.get()) {
        succeeded = true;
        foundNewOptimalPathEvent.accept(foundPath);
        optimalLength.set(foundPath.getLength());
      }
    }
  }

  public boolean isCancelled() {
    return runningStatus.equals(RunningStatus.CANCELLED);
  }

  public boolean isCompleted() {
    return runningStatus.equals(RunningStatus.COMPLETED);
  }

  public boolean isDone() {
    return runningStatus.equals(RunningStatus.COMPLETED) || runningStatus.equals(RunningStatus.CANCELLED);
  }

  public boolean isSuccessful() {
    return succeeded;
  }

  public final Map<D, Set<Link<T, D>>> collectAllEntryDomains() {
    Map<D, Set<Link<T, D>>> entryDomains = new HashMap<>();
    for (Link<T, D> link : links) {
      entryDomains.putIfAbsent(link.getDestination().getDomain(), new HashSet<>());
      entryDomains.get(link.getDestination().getDomain()).add(link);
    }
    return entryDomains;
  }

  /**
   * For every domain, get a set of links that have destinations to that domain.
   * Part of Step 1 of high-level search algorithm.
   *
   * @return a map of every domain to the set of all viable links
   */
  public final Map<D, Set<Link<T, D>>> collectEntryDomains(List<Link<T, D>> links) {
    Map<D, Set<Link<T, D>>> entryDomains = new HashMap<>();
    for (Link<T, D> link : links) {
      entryDomains.putIfAbsent(link.getDestination().getDomain(), new HashSet<>());
      entryDomains.get(link.getDestination().getDomain()).add(link);
    }
    return entryDomains;
  }

  public final Map<D, Set<Link<T, D>>> collectAllExitDomains() {
    Map<D, Set<Link<T, D>>> exitDomains = new HashMap<>();
    for (Link<T, D> link : links) {
      exitDomains.putIfAbsent(link.getOrigin().getDomain(), new HashSet<>());
      exitDomains.get(link.getOrigin().getDomain()).add(link);
    }
    return exitDomains;
  }


  /**
   * For every domain, get a set of links that have origins from that domain.
   * Part of Step 1 of high-level search algorithm.
   *
   * @return a map of every domain to the set of all viable links
   */
  public final Map<D, Set<Link<T, D>>> collectExitDomains(List<Link<T, D>> links) {
    Map<D, Set<Link<T, D>>> exitDomains = new HashMap<>();
    for (Link<T, D> link : links) {
      exitDomains.putIfAbsent(link.getOrigin().getDomain(), new HashSet<>());
      exitDomains.get(link.getOrigin().getDomain()).add(link);
    }
    return exitDomains;
  }

  /**
   * Cancel this search. If it has already succeeded, no effect.
   */
  public void cancel() {
    if (!this.runningStatus.equals(RunningStatus.COMPLETED)) {
      this.runningStatus = RunningStatus.CANCELLED;
    }
  }

  public void search(T origin, T destination) {
    runningStatus = RunningStatus.RUNNING;
    succeeded = false;

    // Stage 1 - Only keep the links that may be helpful for finding this path
    List<Link<T, D>> filteredLinks = filterLinks(origin, destination, links);

    // Stage 2 & 3- Create graph based on paths made from local breadth first searches
    findOptimalPath(origin, destination, filteredLinks);
  }

  public void searchCacheable() {
    runningStatus = RunningStatus.RUNNING;
    succeeded = false;

    TrailSearchRequestQueue<T, D> queue = new TrailSearchRequestQueue<>();
    Map<D, Set<Link<T, D>>> entryDomains = collectAllEntryDomains();
    Map<D, Set<Link<T, D>>> exitDomains = collectAllExitDomains();
    Set<D> allDomains = new HashSet<>();
    allDomains.addAll(entryDomains.keySet());
    allDomains.addAll(exitDomains.keySet());
    Trail<T, D> trail;
    for (D domain : allDomains) {
      if (entryDomains.containsKey(domain)) {
        for (Link<T, D> entry : entryDomains.get(domain)) {
          if (exitDomains.containsKey(domain)) {
            for (Link<T, D> exit : exitDomains.get(domain)) {
              if (entry.getOrigin().equals(exit.getDestination())) {
                continue;  // We don't want to use link <-> link if they just come back to the same spot
              }
              queueTrailRequestIfNotCached(null, null, queue,
                  new TrailSearchRequest<>(entry.getDestination(), exit.getOrigin(),
                      null, null,
                      () -> false, true));
            }
          }
        }
      }
    }

    TrailSearch<T, D> search = new TrailSearch<>();
    AtomicReference<TrailSearchRequest<T, D>> request = new AtomicReference<>();
    while (!queue.isEmpty()) {
      trail = queue.popAndRunLinkRequest(search, modes, req -> {
        request.set(req);
        startTrailSearchCallback.accept(req.getOrigin(), req.getDestination());
      });
      if (trail != null) {
        finishTrailSearchCallback.accept(request.get().getOrigin(), request.get().getDestination(), trail.getLength());
        trailCache.put(request.get().getOrigin(), request.get().getDestination(), modeTypes, trail);
      }
    }

    runningStatus = RunningStatus.COMPLETED;
    succeeded = true;
  }

}
