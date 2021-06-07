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

import edu.whimc.indicator.common.search.tracker.BlankSearchTracker;
import edu.whimc.indicator.common.search.tracker.SearchTracker;

import java.util.HashMap;
import java.util.Map;

public abstract class Mode<T extends Cell<T, D>, D> {

  private Map<T, Double> map = new HashMap<>();
  private SearchTracker<T, D> tracker = new BlankSearchTracker<>();

  public final Map<T, Double> getDestinations(T origin, SearchTracker<T, D> tracker) {
    this.map = new HashMap<>();
    this.tracker = tracker;
    collectDestinations(origin);
    return map;
  }

  protected final void accept(T destination, double distance) {
    this.map.put(destination, distance);
    tracker.acceptResult(destination, SearchTracker.Result.SUCCESS, getType());
  }

  protected final void reject(T destination) {
    tracker.acceptResult(destination, SearchTracker.Result.FAILURE, getType());
  }

  protected abstract void collectDestinations(T origin);

  public abstract ModeType getType();

}
