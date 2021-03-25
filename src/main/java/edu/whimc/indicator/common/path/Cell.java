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

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Cell<T extends Cell<T, D>, D> implements Locatable<T, D> {

  @Getter protected final int x;
  @Getter protected final int y;
  @Getter protected final int z;
  protected final D domain;

  public Cell(int x, int y, int z, @NotNull D domain) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.domain = Objects.requireNonNull(domain);
  }

  @Override
  abstract public double distanceToSquared(T other);

  @Override
  public D getDomain() {
    return this.domain;
  }

}
