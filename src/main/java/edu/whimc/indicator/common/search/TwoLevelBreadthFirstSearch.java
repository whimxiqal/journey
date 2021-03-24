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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.cache.TrailCache;
import edu.whimc.indicator.common.path.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TwoLevelBreadthFirstSearch<T extends Locatable<T, D>, D> implements Search<T, D> {

  private final List<Link<T, D>> links = Lists.newLinkedList();
  private final List<Mode<T, D>> modes = Lists.newLinkedList();
  private final ModeTypeGroup modeTypes = new ModeTypeGroup();
  private final TrailCache<T, D> trailCache;

  @Getter
  private boolean cancelled = false;

  @Getter
  private boolean done = false;

  // Callbacks
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
  private BiConsumer<T, T> finishTrailSearchCallback = (l1, l2) -> {
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

  private Trail<T, D> findShortestTrail(TrailSearch<T, D> bfs,
                                        T origin,
                                        T destination,
                                        Supplier<Boolean> cancellation,
                                        boolean shouldCache) {
    // Check if this trail is cached
    Trail<T, D> trail = trailCache.get(origin, destination, modeTypes);

    // It is cached
    if (trail != null) {
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
        // All good, return the cached trail
        return trail;
      }
      // The cache didn't work, do a new search
    }

    // Start a new search
    startTrailSearchCallback.accept(origin, destination);
    trail = bfs.findShortestTrail(origin, destination, modes, cancellation);
    finishTrailSearchCallback.accept(origin, destination);

    if (shouldCache) {
      trailCache.put(origin, destination, modeTypes, trail);
    }
    return trail;
  }

  private Path<T, D> findMinimumPath(T origin, T destination, List<Link<T, D>> links) {
    // Step 1 - organize filtered links into entry and exit points in every domain
    Map<D, Set<Link<T, D>>> entryDomains = collectEntryDomains();
    Map<D, Set<Link<T, D>>> exitDomains = collectExitDomains();

    // Step 2 - Initialize local-domain search class
    TrailSearch<T, D> trailSearch = new TrailSearch<>();
    trailSearch.setStepCallback(trailSearchStepCallback);
    trailSearch.setVisitationCallback(trialSearchVisitationCallback);

    // Step 3 - Map of paths
    // origin -> link
    Map<Link<T, D>, Trail<T, D>> originTrails = findOriginTrails(trailSearch, origin, exitDomains);
    // link -> destination
    Map<Link<T, D>, Trail<T, D>> destinationTrails = findDestinationTrails(trailSearch, destination, entryDomains);
    // link -> link
    Table<Link<T, D>, Link<T, D>, Trail<T, D>> linkTrails = findLinkTrails(trailSearch, entryDomains, exitDomains);

    // Step 4 - Set up graph
    PathEdgeGraph<T, D> graph = new PathEdgeGraph<>();
    // Nodes
    PathEdgeGraph.Node originNode = graph.generateNode();
    PathEdgeGraph.Node destinationNode = graph.generateNode();
    Map<Link<T, D>, PathEdgeGraph.Node> linkNodeMap = new HashMap<>();
    links.forEach(link -> linkNodeMap.put(link, graph.generateLinkNode(link)));
    // Edges
    originTrails.forEach((link, p) -> graph.addEdge(originNode, linkNodeMap.get(link), p));
    destinationTrails.forEach((link, p) -> graph.addEdge(linkNodeMap.get(link), destinationNode, p));
    linkTrails.cellSet().forEach((cell) -> graph.addEdge(
        linkNodeMap.get(cell.getRowKey()),
        linkNodeMap.get(cell.getColumnKey()),
        cell.getValue()));
    // Origin to Endpoint edge if they are in the same domain
    if (origin.getDomain().equals(destination.getDomain())) {
      try {
        Trail<T, D> foundPath = findShortestTrail(trailSearch, origin, destination, this::isCancelled, false);
        if (foundPath != null) {
          graph.addEdge(originNode, destinationNode, foundPath);
        }
      } catch (TrailSearch.MemoryCapacityException e) {
        memoryCapacityErrorCallback.accept(origin, destination);
      }
    }

    // Step 5 - Solve graph - Find the minimum path from the domain graph
    return graph.findMinimumPath(originNode, destinationNode);
  }

  /**
   * For every domain, get a set of links that have destinations to that domain.
   * Part of Step 1 of high-level search algorithm.
   *
   * @return a map of every domain to the set of all viable links
   */
  public final Map<D, Set<Link<T, D>>> collectEntryDomains() {
    Map<D, Set<Link<T, D>>> entryDomains = new HashMap<>();
    for (Link<T, D> link : links) {
      entryDomains.putIfAbsent(link.getDestination().getDomain(), new HashSet<>());
      entryDomains.get(link.getDestination().getDomain()).add(link);
    }
    return entryDomains;
  }

  /**
   * For every domain, get a set of links that have origins from that domain.
   * Part of Step 1 of high-level search algorithm.
   *
   * @return a map of every domain to the set of all viable links
   */
  public final Map<D, Set<Link<T, D>>> collectExitDomains() {
    Map<D, Set<Link<T, D>>> exitDomains = new HashMap<>();
    for (Link<T, D> link : links) {
      exitDomains.putIfAbsent(link.getOrigin().getDomain(), new HashSet<>());
      exitDomains.get(link.getOrigin().getDomain()).add(link);
    }
    return exitDomains;
  }

  /**
   * Find all trails that go from the origin
   * Part of Step 3 of high-level search algorithm.
   *
   * @param trailSearch the local search object
   * @param origin      the origin from which the trails will be found
   * @param exitDomains for every domain, all links which exit the domain. Only the domain of the origin will be used.
   * @return map of all links to trails of which every link is
   * mapped to the trail which goes from the overall origin to the origin
   * of the link
   */
  public final Map<Link<T, D>, Trail<T, D>> findOriginTrails(TrailSearch<T, D> trailSearch, T origin, Map<D, Set<Link<T, D>>> exitDomains) {
    Trail<T, D> trail;
    Map<Link<T, D>, Trail<T, D>> originTrails = new HashMap<>();
    if (exitDomains.containsKey(origin.getDomain())) {
      for (Link<T, D> exit : exitDomains.get(origin.getDomain())) {
        try {
          trail = findShortestTrail(trailSearch, origin, exit.getOrigin(), this::isCancelled, false);
          if (trail != null) {
            originTrails.put(exit, trail);
          }
        } catch (TrailSearch.MemoryCapacityException e) {
          memoryCapacityErrorCallback.accept(origin, exit.getOrigin());
        }
      }
    }
    return originTrails;
  }

  /**
   * Find all trails that go to the destination
   * Part of Step 3 of high-level search algorithm.
   *
   * @param trailSearch the local search object
   * @param destination      the origin from which the trails will be found
   * @param entryDomains for every domain, all links which enter the domain. Only the domain of the destination will be used.
   * @return map of all links to trails of which every link is
   * mapped to the trail which goes from the destination of the link to the overall destination
   */
  public Map<Link<T, D>, Trail<T, D>> findDestinationTrails(TrailSearch<T, D> trailSearch, T destination, Map<D, Set<Link<T, D>>> entryDomains) {
    Trail<T, D> trail;
    Map<Link<T, D>, Trail<T, D>> destinationTrails = new HashMap<>();
    if (entryDomains.containsKey(destination.getDomain())) {
      for (Link<T, D> entry : entryDomains.get(destination.getDomain())) {
        try {
          trail = findShortestTrail(trailSearch, entry.getDestination(), destination, this::isCancelled, false);
          if (trail != null) {
            destinationTrails.put(entry, trail);
          }
        } catch (TrailSearch.MemoryCapacityException e) {
          memoryCapacityErrorCallback.accept(entry.getDestination(), destination);
        }
      }
    }
    return destinationTrails;
  }

  /**
   * Find all trails that go between links
   * Part of Step 3 of high-level search algorithm.
   *
   * @param trailSearch the local search object
   * @param entryDomains for every domain, all links which enter the domain.
   * @param exitDomains for every domain, all links which exit the domain.
   * @return table of all links to trails of which every link-link key is
   * mapped to the trail which goes from the destination of the first link
   * to the origin of the other link.
   */
  public Table<Link<T, D>, Link<T, D>, Trail<T, D>> findLinkTrails(TrailSearch<T, D> trailSearch,
                                                                   Map<D, Set<Link<T, D>>> entryDomains,
                                                                   Map<D, Set<Link<T, D>>> exitDomains) {
    // put all domains into one set
    Set<D> allDomains = new HashSet<>();
    allDomains.addAll(entryDomains.keySet());
    allDomains.addAll(exitDomains.keySet());

    Trail<T, D> trail;
    Table<Link<T, D>, Link<T, D>, Trail<T, D>> linkTrails = HashBasedTable.create();
    for (D domain : allDomains) {
      if (entryDomains.containsKey(domain)) {
        for (Link<T, D> entry : entryDomains.get(domain)) {
          if (exitDomains.containsKey(domain)) {
            for (Link<T, D> exit : exitDomains.get(domain)) {
              if (entry.isReverse(exit)) {
                continue;
              }
              try {
                trail = findShortestTrail(trailSearch, entry.getDestination(), exit.getOrigin(), this::isCancelled, true);
                if (trail != null) {
                  linkTrails.put(entry, exit, trail);
                }
              } catch (TrailSearch.MemoryCapacityException e) {
                memoryCapacityErrorCallback.accept(entry.getDestination(), exit.getOrigin());
              }
            }
          }
        }
      }
    }
    return linkTrails;
  }

  public void setCancelled(boolean cancelled) {
    if (cancelled) {
      this.cancelled = true;
      this.done = true;
    } else {
      this.cancelled = false;
    }
  }

  @Override
  public Path<T, D> findPath(T origin, T destination) {
    done = false;
    // Stage 1 - Only keep the links that may be helpful for finding this path
    List<Link<T, D>> filteredLinks = filterLinks(origin, destination, links);

    // Stage 2 & 3- Create graph based on paths made from local breadth first searches
    Path<T, D> path = findMinimumPath(origin, destination, filteredLinks);
    done = true;
    return path;
  }

}
