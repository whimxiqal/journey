/*
 * MIT License
 *
 * Copyright (c) 2020 Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package edu.whimc.indicator.config;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Setting<T> {

  private final String path;
  private final T defaultValue;
  private T value;
  private final Class<T> clazz;

  Setting(@NotNull String path, @NotNull T defaultValue, @NotNull Class<T> clazz) {
    if (!clazz.isInstance(defaultValue)) {
      throw new IllegalArgumentException("The value must match the class type");
    }
    this.path = Objects.requireNonNull(path);
    this.defaultValue = Objects.requireNonNull(defaultValue);
    this.value = Objects.requireNonNull(defaultValue);
    this.clazz = clazz;
  }

  @NotNull
  public String getPath() {
    return path;
  }

  @NotNull
  public T getDefaultValue() {
    return clazz.cast(defaultValue);
  }

  public void setValue(@NotNull T value) {
    this.value = Objects.requireNonNull(value);
  }

  @NotNull
  public T getValue() {
    return value;
  }

}
