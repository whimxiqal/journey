/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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

package me.pietelite.journey.common.search.flag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class FlagSet {

  private final Set<Flag> flags = new HashSet<>();
  private final Map<ValueFlag<?>, Object> valueFlags = new HashMap<>();

  public void addFlag(Flag flag) {
    if (flag instanceof ValueFlag) {
      throw new RuntimeException();  // programmer error
    }
    this.flags.add(flag);
  }

  public <T> void addValueFlag(ValueFlag<T> flag, T value) {
    this.valueFlags.put(flag, value);
  }

  public boolean hasFlag(Flag flag) {
    return this.flags.contains(flag) || this.valueFlags.containsKey(flag);
  }

  public <T> T valueOf(ValueFlag<T> flag) {
    Object val = valueFlags.get(flag);
    if (val == null) {
      throw new RuntimeException(); // programmer error
    }
    return (T) val;  // cast exception -> programmer error
  }

  public <T> T valueOrGetDefault(ValueFlag<T> flag, Supplier<T> defaultValue) {
    if (valueFlags.containsKey(flag)) {
      return valueOf(flag);
    } else {
      return defaultValue.get();
    }
  }

  public void forEach(BiConsumer<Flag, Optional<String>> consumer) {
    flags.forEach(flag -> consumer.accept(flag, Optional.empty()));
    valueFlags.forEach((key, value) -> consumer.accept(key, Optional.of(((ValueFlag<Object>) key).printValue(value))));
  }

}
