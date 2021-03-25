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

package edu.whimc.indicator.common.path;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public final class Path<T extends Locatable<T, D>, D> {

  private final List<Trail<T, D>> trails = Lists.newArrayList();
  private final List<Link<T, D>> domainLinks = Lists.newArrayList();

  public void addLinkedTrail(Trail<T, D> trail, Link<T, D> link) {
    this.trails.add(trail);
    this.domainLinks.add(link);
  }

  public boolean addFinalTrail(Trail<T, D> trail) {
    if (trails.size() > domainLinks.size()) {
      // There are more trails than links to a final trail must have already been added.
      return false;
    }
    this.trails.add(trail);
    return true;
  }

  public T getOrigin() {
    List<Step<T, D>> steps = trails.get(0).getSteps();
    if (trails.isEmpty() || steps.isEmpty()) {
      return null;
    }
    return steps.get(0).getLocatable();
  }

  public T getDestination() {
    List<Step<T, D>> steps = trails.get(trails.size() - 1).getSteps();
    if (trails.isEmpty() || steps.isEmpty()) {
      return null;
    }
    return steps.get(steps.size() - 1).getLocatable();
  }

  public List<Trail<T, D>> getTrailsCopy() {
    return new ArrayList<>(trails);
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
    for (Trail<T, D> trail : trails) {
      allSteps.addAll(trail.getSteps());
    }
    return allSteps;
  }

}
