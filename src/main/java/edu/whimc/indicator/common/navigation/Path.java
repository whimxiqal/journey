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
import java.io.Serializable;
import java.util.ArrayList;
import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

@Data
public final class Path<T extends Locatable<T, D>, D> implements Serializable {

  @NonNull
  private final ArrayList<Step<T, D>> steps;
  private final double length;

  public static <A extends Locatable<A, B>, B> Path<A, B> INVALID() {
    return new Path<>(Lists.newArrayList(), Double.MAX_VALUE);
  }

  public static <A extends Locatable<A, B>, B> boolean isValid(@Nullable Path<A, B> path) {
    return path != null && Double.compare(path.getLength(), Double.MAX_VALUE) < 0;
  }

  public T getOrigin() {
    if (steps.isEmpty()) return null;
    return steps.get(0).getLocatable();
  }

  public T getDestination() {
    if (steps.isEmpty()) return null;
    return steps.get(steps.size() - 1).getLocatable();
  }

}
