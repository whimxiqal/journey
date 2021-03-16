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

package edu.whimc.indicator.api.path;

import edu.whimc.indicator.spigot.path.NetherLink;

public interface Link<T extends Locatable<T, D>, D> {

  T getOrigin();

  T getDestination();

  Completion<T, D> getCompletion();

  double weight();

  /**
   * Verify that this link is correct.
   *
   * @return true if this link still exists
   */
  boolean verify();

  default boolean isReverse(Object o) {
    if (this == o) return false;
    if (o == null || getClass() != o.getClass()) return false;
    NetherLink that = (NetherLink) o;
    return getOrigin().equals(that.getDestination()) && getDestination().equals(that.getOrigin());
  }

}
