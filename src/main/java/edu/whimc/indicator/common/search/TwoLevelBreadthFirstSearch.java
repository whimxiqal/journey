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
import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.cache.TrailCache;
import edu.whimc.indicator.common.path.*;
import edu.whimc.indicator.common.util.TriConsumer;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TwoLevelBreadthFirstSearch<T extends Locatable<T, D>, D> {

  private final List<Link<T, D>> links = Lists.newLinkedList();
  private final List<Mode<T, D>> modes = Lists.newLinkedList();
  private final ModeTypeGroup modeTypes = new ModeTypeGroup();
  private final TrailSearchRequestQueue<T, D> trailSearchRequestQueue = new TrailSearchRequestQueue<>();
  private final TrailCache<T, D> trailCache;

  @Getter
  private boolean cancelled = false;

  @Getter
  private boolean done = false;

  @Getter
  private boolean successful = false;

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
  private List<Link<T, D>> filterLinks(T origin, T destination, List<Link<T, D>> links) {
    DomainGraph<D> graph = new DomainGraph<>();
    links.forEach(link -> graph.addEdge(link.getOrigin().getDomain(), link.getDestination().getDomain()));
    Set<D> onPath = graph.domainsOnConnectingPath(origin.getDomain(), destination.getDomain());
    return links.stream()
        .filter(link -> onPath.contains(link.getOrigin().getDomain())
            && onPath.contains(link.getDestination().getDomain()))
        .collect(Collectors.toList());
  }

  private void queueTrailRequest(PathEdgeGraph<T, D> pathEdgeGraph,
      T origin,
                                                  T destination,
                                                  PathEdgeGraph.Node originNode,
                                                  PathEdgeGraph.Node destinationNode,
                                                  Supplier<Boolean> cancellation,
                                                  boolean shouldCache) {
    Trail<T, D> trail = trailCache.get(origin, destination, modeTypes);

    if (trail == null) {
      // See if we can get a trail that was cached with a subgroup of these mode types
      trail = trailCache.getAnyMatching(origin, destination, modeTypes);
    }

    if (trail == null) {
      // There is no valid cached trail, so just queue a search
      trailSearchRequestQueue.add(new TrailSearchRequest<>(origin, destination, originNode, destinationNode, modes, cancellation, shouldCache));
      return;
    }

    // There's a cached trail!
    if (trail.getSteps().isEmpty()) {
      // It's an invalid, no verification needed
      return;
    }

    // Verify this trail still works
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
    if (validStep) {
      // All good, add the cached trail to the graph
      pathEdgeGraph.addEdge(originNode, destinationNode, trail);
    }
    // The cache didn't work, do a new search

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
    // Nodes
    PathEdgeGraph.Node originNode = graph.generateNode();
    PathEdgeGraph.Node destinationNode = graph.generateNode();

    Map<Link<T, D>, PathEdgeGraph.Node> linkNodeMap = new HashMap<>();
    links.forEach(link -> linkNodeMap.put(link, graph.generateLinkNode(link)));
    // Edges

    // Origin to Endpoint edge if they are in the same domain
    if (origin.getDomain().equals(destination.getDomain())) {
      try {
        queueTrailRequest(graph,
            origin, destination,
            originNode, destinationNode,
            this::isCancelled, false);
      } catch (TrailSearch.MemoryCapacityException e) {
        memoryCapacityErrorCallback.accept(origin, destination);
      }
    }

    // QUEUE TRAILS
    // Queue trails: origin -> link
    if (exitDomains.containsKey(origin.getDomain())) {
      for (Link<T, D> exit : exitDomains.get(origin.getDomain())) {
        queueTrailRequest(graph,
            origin, exit.getOrigin(),
            originNode, linkNodeMap.computeIfAbsent(exit, graph::generateLinkNode),
            this::isCancelled, false);
      }
    }
    // Queue trails: link -> destination
    if (entryDomains.containsKey(destination.getDomain())) {
      for (Link<T, D> entry : entryDomains.get(destination.getDomain())) {
        queueTrailRequest(graph,
            entry.getDestination(), destination,
            linkNodeMap.computeIfAbsent(entry, graph::generateLinkNode), destinationNode,
            this::isCancelled, false);
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
              queueTrailRequest(graph,
                  entry.getDestination(), exit.getOrigin(),
                  linkNodeMap.computeIfAbsent(entry, graph::generateLinkNode),
                  linkNodeMap.computeIfAbsent(exit, graph::generateLinkNode),
                  this::isCancelled, true);
            }
          }
        }
      }
    }

    // Node mappings to make sure that we add to edges to the global graph correctly

    trailSearchRequestQueue.sortByEstimatedLength();

    double optimalLength = Double.MAX_VALUE;
    TrailSearchRequest<T, D> trailRequest;
    Trail<T, D> latestFoundTrail;
    Path<T, D> foundPath;
    while (!trailSearchRequestQueue.isEmpty()) {

      // Start search from the queue
      trailRequest = trailSearchRequestQueue.pop();
      if (trailRequest.getOrigin().distanceToSquared(trailRequest.getDestination()) > optimalLength * optimalLength) {
        // We have already found a path that has a shorter distance than this trail,
        //  so there's no way that this is helpful
        continue;
      }

      startTrailSearchCallback.accept(trailRequest.getOrigin(), trailRequest.getDestination());
      try {
        latestFoundTrail = trailSearch.findOptimalTrail(trailRequest);
      } catch (TrailSearch.MemoryCapacityException e) {
        memoryCapacityErrorCallback.accept(trailRequest.getOrigin(), trailRequest.getDestination());
        if (trailRequest.isCacheable()) {
          trailCache.put(trailRequest.getOrigin(), trailRequest.getDestination(), modeTypes, Trail.INVALID());
        }
        continue;
      }
      finishTrailSearchCallback.accept(trailRequest.getOrigin(), trailRequest.getDestination(), latestFoundTrail.getLength());

      if (trailRequest.isCacheable()) {
        trailCache.put(trailRequest.getOrigin(), trailRequest.getDestination(), modeTypes, latestFoundTrail);
      }

      graph.addEdge(trailRequest.getOriginNode(), trailRequest.getDestinationNode(), latestFoundTrail);

      // Step 5 - Solve graph - Find the minimum path from the domain graph
      foundPath = graph.findMinimumPath(originNode, destinationNode);
      if (foundPath != null && foundPath.getLength() < optimalLength) {
        successful = true;
        foundNewOptimalPathEvent.accept(foundPath);
        optimalLength = foundPath.getLength();
      }
    }
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

  public void setCancelled(boolean cancelled) {
    if (cancelled) {
      this.cancelled = true;
      this.done = true;
    } else {
      this.cancelled = false;
    }
  }

  public CompletableFuture<Void> search(T origin, T destination) {
    done = false;
    // Stage 1 - Only keep the links that may be helpful for finding this path
    List<Link<T, D>> filteredLinks = filterLinks(origin, destination, links);

    // Stage 2 & 3- Create graph based on paths made from local breadth first searches
    return CompletableFuture.runAsync(() -> {
      findOptimalPath(origin, destination, filteredLinks);
      done = true;
    });
  }

}
