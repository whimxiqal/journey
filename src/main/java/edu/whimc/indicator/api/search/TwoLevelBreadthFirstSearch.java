package edu.whimc.indicator.api.search;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import lombok.Setter;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TwoLevelBreadthFirstSearch<T extends Locatable<T, D>, D> implements Search<T> {

  private List<Link<T, D>> links = Lists.newLinkedList();
  private List<Mode<T, D>> modes = Lists.newLinkedList();

  @Setter
  private Consumer<T> localSearchVisitationCallback = loc -> {};
  @Setter
  private Consumer<T> localSearchStepCallback = loc -> {};
  @Setter
  private Runnable finishLocalSearchCallback = () -> {};
  @Setter
  private Runnable memoryErrorCallback = () -> {};

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

  private List<T> findLocalShortestPath(LocalBreadthFirstSearch<T, D> bfs, T origin, T destination, List<Mode<T, D>> modes) {
    List<T> path = bfs.findShortestPath(origin, destination, modes);
    finishLocalSearchCallback.run();
    return path;
  }

  private List<List<T>> findMinimumPath(T origin, T destination, List<Link<T, D>> links) {
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
    LocalBreadthFirstSearch<T, D> bfs = new LocalBreadthFirstSearch<>();
    bfs.setStepCallback(localSearchStepCallback);
    bfs.setVisitationCallback(localSearchVisitationCallback);

    List<T> path;
    // Map of paths: origin -> link
    Map<Link<T, D>, List<T>> originPaths = new HashMap<>();
    if (domainExits.containsKey(origin.getDomain())) {
      for (Link<T, D> exit : domainExits.get(origin.getDomain())) {
        try {
          path = findLocalShortestPath(bfs, origin, exit.getOrigin(), modes);
          if (path != null) {
            originPaths.put(exit, path);
          }
        } catch (LocalBreadthFirstSearch.MemoryCapacityException e) {
          memoryErrorCallback.run();
        }
      }
    }

    // Map of paths: link -> destination
    Map<Link<T, D>, List<T>> destinationPaths = new HashMap<>();
    if (domainEntries.containsKey(destination.getDomain())) {
      for (Link<T, D> entry : domainEntries.get(destination.getDomain())) {
        try {
          path = findLocalShortestPath(bfs, entry.getDestination(), destination, modes);
          if (path != null) {
            destinationPaths.put(entry, path);
          }
        } catch (LocalBreadthFirstSearch.MemoryCapacityException e) {
          memoryErrorCallback.run();
        }
      }
    }

    // Table of paths: link -> link
    Table<Link<T, D>, Link<T, D>, List<T>> linkPaths = HashBasedTable.create();
    for (D domain : domains) {
      if (domainEntries.containsKey(domain)) {
        for (Link<T, D> entry : domainEntries.get(domain)) {
          if (domainExits.containsKey(domain)) {
            for (Link<T, D> exit : domainExits.get(domain)) {
              try {
                path = findLocalShortestPath(bfs, entry.getDestination(), exit.getOrigin(), modes);
                if (path != null) {
                  linkPaths.put(entry, exit, path);
                }
              } catch (LocalBreadthFirstSearch.MemoryCapacityException e) {
                memoryErrorCallback.run();
              }
            }
          }
        }
      }
    }

    PathEdgeGraph<T, D> graph = new PathEdgeGraph<>();

    // Nodes
    PathEdgeGraph.Node originNode = new PathEdgeGraph.Node();
    PathEdgeGraph.Node destinationNode = new PathEdgeGraph.Node();

    Map<Link<T, D>, PathEdgeGraph.Node> linkNodeMap = new HashMap<>();
    links.forEach(link -> {
      PathEdgeGraph.Node linkNode = new PathEdgeGraph.Node();
      linkNodeMap.put(link, linkNode);
    });

    // Edges
    originPaths.forEach((link, p) -> graph.addEdge(originNode, linkNodeMap.get(link), p));
    destinationPaths.forEach((link, p) -> graph.addEdge(linkNodeMap.get(link), destinationNode, p));
    linkPaths.cellSet().forEach((cell) -> graph.addEdge(
        linkNodeMap.get(cell.getRowKey()),
        linkNodeMap.get(cell.getColumnKey()),
        cell.getValue()));
    // Origin to Destination edge if they are in the same domain
    if (origin.getDomain().equals(destination.getDomain())) {
      List<T> foundPath = bfs.findShortestPath(origin, destination, modes);
      if (foundPath != null) {
        graph.addEdge(originNode, destinationNode, foundPath);
      }
    }

    // Stage 3 - Find the minimum path from the domain graph
    return graph.findMinimumPath(originNode, destinationNode);
  }


  @Override
  public List<T> findPath(T origin, T destination) {

    // Stage 1 - Only keep the links that may be helpful for finding this path
    List<Link<T, D>> filteredLinks = filterLinks(origin, destination, links);
    if (filteredLinks.size() == 0) {
      return null;
    }

    // Stage 2 & 3- Create graph based on paths made from local breadth first searches
    List<List<T>> paths = findMinimumPath(origin, destination, filteredLinks);
    List<T> minimumPath = Lists.newLinkedList();
    if (paths == null) {
      return null;
    }

    // Finalize output
    paths.forEach(minimumPath::addAll);
    return minimumPath;

  }

}
