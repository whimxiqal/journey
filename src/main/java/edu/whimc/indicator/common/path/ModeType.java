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

import edu.whimc.indicator.common.util.PrimeUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ModeType {

  public static final ModeType NONE = ModeType.of("none", false);  // Origin, for example
  private static PrimeUtil primeUtil = null;

  @Getter
  private final String id;

  @Getter
  private final int primeIdentifier;

  @Getter
  private final boolean common;

  private ModeType(@NotNull String id, boolean common, int primeIdentifier) {
    this.id = id;
    this.common = common;
    this.primeIdentifier = primeIdentifier;
  }

  public static ModeType of(String id, boolean common) {
    if (primeUtil == null) {
      primeUtil = new PrimeUtil();
    }
    return new ModeType(id, common, primeUtil.getNextPrime());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ModeType modeType = (ModeType) o;
    return id.equals(modeType.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "ModeType{" + "id='"
        + id + '\'' + '}';
  }
}
