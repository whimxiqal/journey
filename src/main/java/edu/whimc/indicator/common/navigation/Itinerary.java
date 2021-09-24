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

package edu.whimc.indicator.common.navigation;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public final class Itinerary<T extends Locatable<T, D>, D> {

  private final List<Path<T, D>> paths = Lists.newArrayList();
  private final List<Link<T, D>> domainLinks = Lists.newArrayList();
  @Getter
  double length = 0;

  public void addLinkedTrail(Path<T, D> path, Link<T, D> link) {
    this.paths.add(path);
    this.domainLinks.add(link);
    length += path.getLength();
  }

  public boolean addFinalTrail(Path<T, D> path) {
    if (paths.size() > domainLinks.size()) {
      // There are more paths than links to a final path must have already been added.
      return false;
    }
    this.paths.add(path);
    length += path.getLength();
    return true;
  }

  public T getOrigin() {
    List<Step<T, D>> steps = paths.get(0).getSteps();
    if (paths.isEmpty() || steps.isEmpty()) {
      return null;
    }
    return steps.get(0).getLocatable();
  }

  public T getDestination() {
    List<Step<T, D>> steps = paths.get(paths.size() - 1).getSteps();
    if (paths.isEmpty() || steps.isEmpty()) {
      return null;
    }
    return steps.get(steps.size() - 1).getLocatable();
  }

  public List<Path<T, D>> getTrailsCopy() {
    return new ArrayList<>(paths);
  }

  public List<Link<T, D>> getDomainLinksCopy() {
    return new ArrayList<>(domainLinks);
  }

  /**
   * Creates an (array) list of every step, ignoring links.
   *
   * @return all steps in a list
   */
  public List<Step<T, D>> getAllSteps() {
    ArrayList<Step<T, D>> allSteps = new ArrayList<>();
    for (Path<T, D> path : paths) {
      allSteps.addAll(path.getSteps());
    }
    return allSteps;
  }

}
