/*
 * MIT License
 *
 * Copyright (c) whimxiqal
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

package net.whimxiqal.journey.search.flag;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class Flag<T> {

  private final String name;
  private final Supplier<T> defaultValue;
  private final String permission;
  private final Class<T> clazz;

  public Flag(String name, Supplier<T> defaultValue, String permission, Class<T> clazz) {
    this.name = Objects.requireNonNull(name);
    this.defaultValue = defaultValue;
    this.permission = permission;
    this.clazz = clazz;
  }

  public final String name() {
    return name;
  }

  public String printValue(T val) {
    return val.toString();
  }

  public final T defaultValue() {
    return defaultValue.get();
  }

  public final String permission() {
    return permission;
  }

  public boolean valid(T value) {
    return true;
  }

  /**
   * Suggested values for the user to set on this flag.
   *
   * @return list of suggested values
   */
  public List<? extends T> suggestedValues() {
    return Collections.emptyList();
  }

  @Override
  public final int hashCode() {
    return this.name.hashCode();
  }

  @Override
  public final boolean equals(Object obj) {
    return (obj instanceof Flag) && ((Flag) obj).name.equals(this.name);
  }

  @Override
  public String toString() {
    return "Flag[" + name + "]";
  }
}
