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

import edu.whimc.indicator.common.IndicatorCommon;
import edu.whimc.indicator.common.search.SearchSession;
import edu.whimc.indicator.common.search.event.ModeFailureEvent;
import edu.whimc.indicator.common.search.event.ModeSuccessEvent;
import edu.whimc.indicator.common.search.event.SearchDispatcher;
import edu.whimc.indicator.common.search.event.SearchEvent;
import java.util.HashMap;
import java.util.Map;

public abstract class Mode<T extends Cell<T, D>, D> {

  private Map<T, Double> map = new HashMap<>();
  private SearchSession<T, D> session;

  public final Map<T, Double> getDestinations(T origin, SearchSession<T, D> session) {
    this.map = new HashMap<>();
    this.session = session;
    collectDestinations(origin);
    return map;
  }

  protected final void accept(T destination, double distance) {
    this.map.put(destination, distance);
    delay();
    IndicatorCommon.<T, D>getSearchEventDispatcher().dispatch(new ModeSuccessEvent<>(session, destination, getType()));
  }

  protected final void reject(T destination) {
    delay();
    IndicatorCommon.<T, D>getSearchEventDispatcher().dispatch(new ModeFailureEvent<>(session, destination, getType()));
  }

  private void delay() {
    // Delay the algorithm, if requested by implementation of search session
    if (session.getAlgorithmStepDelay() != 0) {
      try {
        Thread.sleep(session.getAlgorithmStepDelay());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  protected abstract void collectDestinations(T origin);

  public abstract ModeType getType();

}
