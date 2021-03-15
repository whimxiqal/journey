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
