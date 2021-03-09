package edu.whimc.indicator.api.search;

import com.google.common.collect.Maps;

import java.util.*;

public class DomainGraph<D> {

  private final Map<D, Set<D>> edges = Maps.newHashMap();

  public void addEdge(D origin, D destination) {
    edges.putIfAbsent(origin, new HashSet<>());
    edges.get(origin).add(destination);
  }

  public Set<D> domainsOnConnectingPath(D origin, D destination) {

    Stack<D> toVisit = new Stack<>();
    Set<D> visited = new HashSet<>();

    // Forward
    Set<D> canReach = new HashSet<>();
    toVisit.add(origin);
    canReach.add(origin);
    D current;
    while (!toVisit.empty()) {
      current = toVisit.pop();
      if (visited.contains(current)) {
        continue;
      }
      visited.add(current);
      canReach.add(current);
      if (edges.containsKey(current)) {
        toVisit.addAll(edges.get(current));
      }
    }

    // Backward
    toVisit.clear();
    visited.clear();
    Map<D, Set<D>> backwardsEdges = Maps.newHashMap();
    edges.forEach((o, ds) -> ds.forEach(d -> {
      backwardsEdges.putIfAbsent(d, new HashSet<>());
      backwardsEdges.get(d).add(o);
    }));
    Set<D> canReachBackwards = new HashSet<>();
    toVisit.add(destination);
    canReachBackwards.add(destination);
    while (!toVisit.empty()) {
      current = toVisit.pop();
      if (visited.contains(current)) {
        continue;
      }
      visited.add(current);
      canReachBackwards.add(current);
      if (backwardsEdges.containsKey(current)) {
        toVisit.addAll(backwardsEdges.get(current));
      }
    }

    // Together
    canReach.retainAll(canReachBackwards);
    return canReach;
  }

}
