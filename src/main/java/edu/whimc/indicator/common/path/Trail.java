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
import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

@Data
public final class Trail<T extends Locatable<T, D>, D> implements Serializable {

  public static <A extends Locatable<A, B>, B> Trail<A, B> INVALID() {
    return new Trail<>(Lists.newArrayList(), Double.MAX_VALUE);
  }

  public static <A extends Locatable<A, B>, B> boolean isValid(@Nullable Trail<A, B> trail) {
    return trail != null && Double.compare(trail.getLength(), Double.MAX_VALUE) < 0;
  }

  @NonNull
  private final ArrayList<Step<T, D>> steps;
  private final double length;

  public T getOrigin() {
    if (steps.isEmpty()) return null;
    return steps.get(0).getLocatable();
  }

  public T getDestination() {
    if (steps.isEmpty()) return null;
    return steps.get(steps.size() - 1).getLocatable();
  }

}
