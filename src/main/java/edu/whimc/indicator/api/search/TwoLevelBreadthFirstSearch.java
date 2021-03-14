package edu.whimc.indicator.api.search;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.api.path.*;
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

  @Setter
  @Getter
  private boolean cancelled = false;

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

  public void registerLink(Link<T, D> link) {
    this.links.add(link);
  }

  public void registerMode(Mode<T, D> mode) {
    this.modes.add(mode);
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
                                        List<Mode<T, D>> modes,
                                        Supplier<Boolean> cancellation) {
    startTrailSearchCallback.accept(origin, destination);
    Trail<T, D> trail = bfs.findShortestTrail(origin, destination, modes, cancellation);
    finishTrailSearchCallback.accept(origin, destination);
    return trail;
  }

  private Path<T, D> findMinimumPath(T origin, T destination, List<Link<T, D>> links) {
    // organize filtered links into entry and exit points in every domain
    Map<D, Set<Link<T, D>>> domainEntries = new HashMap<>();
    Map<D, Set<Link<T, D>>> domainExits = new HashMap<>();

    for (Link<T, D> link : links) {
      domainEntries.putIfAbsent(link.getDestination().getDomain(), new HashSet<>());
      domainEntries.get(link.getDestination().getDomain()).add(link);
      domainExits.putIfAbsent(link.getOrigin().getDomain(), new HashSet<>());
      domainExits.get(link.getOrigin().getDomain()).add(link);
    }

    // Collect all domains
    Set<D> domains = new HashSet<>();
    domains.addAll(domainEntries.keySet());
    domains.addAll(domainExits.keySet());

    // Initialize local-domain search class
    TrailSearch<T, D> trailSearch = new TrailSearch<>();
    trailSearch.setStepCallback(trailSearchStepCallback);
    trailSearch.setVisitationCallback(trialSearchVisitationCallback);

    Trail<T, D> trail;
    // Map of paths: origin -> link
    Map<Link<T, D>, Trail<T, D>> originTrails = new HashMap<>();
    if (domainExits.containsKey(origin.getDomain())) {
      for (Link<T, D> exit : domainExits.get(origin.getDomain())) {
        try {
          trail = findShortestTrail(trailSearch, origin, exit.getOrigin(), modes, this::isCancelled);
          if (trail != null) {
            originTrails.put(exit, trail);
          }
        } catch (TrailSearch.MemoryCapacityException e) {
          memoryCapacityErrorCallback.accept(origin, destination);
        }
      }
    }

    // Map of paths: link -> destination
    Map<Link<T, D>, Trail<T, D>> destinationTrails = new HashMap<>();
    if (domainEntries.containsKey(destination.getDomain())) {
      for (Link<T, D> entry : domainEntries.get(destination.getDomain())) {
        try {
          trail = findShortestTrail(trailSearch, entry.getDestination(), destination, modes, this::isCancelled);
          if (trail != null) {
            destinationTrails.put(entry, trail);
          }
        } catch (TrailSearch.MemoryCapacityException e) {
          memoryCapacityErrorCallback.accept(origin, destination);
        }
      }
    }

    // Table of paths: link -> link
    Table<Link<T, D>, Link<T, D>, Trail<T, D>> linkTrails = HashBasedTable.create();
    for (D domain : domains) {
      if (domainEntries.containsKey(domain)) {
        for (Link<T, D> entry : domainEntries.get(domain)) {
          if (domainExits.containsKey(domain)) {
            for (Link<T, D> exit : domainExits.get(domain)) {
              if (entry.isReverse(exit)) {
                continue;
              }
              try {
                trail = findShortestTrail(trailSearch, entry.getDestination(), exit.getOrigin(), modes, this::isCancelled);
                if (trail != null) {
                  linkTrails.put(entry, exit, trail);
                }
              } catch (TrailSearch.MemoryCapacityException e) {
                memoryCapacityErrorCallback.accept(origin, destination);
              }
            }
          }
        }
      }
    }

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
        Trail<T, D> foundPath = findShortestTrail(trailSearch, origin, destination, modes, this::isCancelled);
        if (foundPath != null) {
          graph.addEdge(originNode, destinationNode, foundPath);
        }
      } catch (TrailSearch.MemoryCapacityException e) {
        memoryCapacityErrorCallback.accept(origin, destination);
      }
    }

    // Stage 3 - Find the minimum path from the domain graph
    return graph.findMinimumPath(originNode, destinationNode);
  }


  @Override
  public Path<T, D> findPath(T origin, T destination) {

    // Stage 1 - Only keep the links that may be helpful for finding this path
    List<Link<T, D>> filteredLinks = filterLinks(origin, destination, links);

    // Stage 2 & 3- Create graph based on paths made from local breadth first searches
    return findMinimumPath(origin, destination, filteredLinks);

  }

}
