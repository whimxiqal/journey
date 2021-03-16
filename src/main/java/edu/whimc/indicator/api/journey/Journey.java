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

package edu.whimc.indicator.api.journey;

import edu.whimc.indicator.api.path.Locatable;
import edu.whimc.indicator.api.path.Step;

import java.util.Collection;

/**
 * Manage information about the traversal of locatables
 * within the game.
 *
 * @param <T> the type of locatable
 * @param <D> the type of domain
 */
public interface Journey<T extends Locatable<T, D>, D> {

  /**
   * Notify this {@link Journey} that the given {@link Locatable}
   * has been visited. This may be called very often, so efficiency
   * is important here.
   *
   * @param locatable the visited locatable
   */
  void visit(T locatable);

  /**
   * Give the next locatables to traverse along the journey.
   *
   * @param count the number of locatables to get
   * @return a collection of locatables of size {@code count}
   */
  Collection<Step<T, D>> next(int count);

  boolean isCompleted();

}
