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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.whimxiqal.journey.search.SearchFlag;

public class FlagSet {

  private final Map<String, FlagPair<?>> flags = new ConcurrentHashMap<>();

  public static FlagSet from(SearchFlag<?>[] flags) {
    FlagSet set = new FlagSet();
    for (SearchFlag<?> flag : flags) {
      switch (flag.type()) {
        case TIMEOUT -> set.addFlag(Flags.TIMEOUT, (int) flag.value());
        case FLY -> set.addFlag(Flags.FLY, (boolean) flag.value());
      }
    }
    return set;
  }

  public <T> void addFlag(Flag<T> flag, T value) {
    flags.put(flag.name(), new FlagPair<>(flag, value));
  }

  public void addFlags(FlagSet other) {
    flags.putAll(other.flags);
  }

  @SuppressWarnings("unchecked")
  public <T> T getValueFor(Flag<T> flag) {
    FlagPair<?> value = flags.get(flag.name());
    if (value == null) {
      return flag.defaultValue();
    }
    return ((FlagPair<T>) value).value();
  }

  public <T> String printValueFor(Flag<T> flag) {
    return flag.printValue(getValueFor(flag));
  }

  public List<FlagPair<?>> flagPairs() {
    return new ArrayList<>(flags.values());
  }

  @Override
  public String toString() {
    return "FlagSet{" + flags + "}";
  }

  public boolean isEmpty() {
    return flags.isEmpty();
  }

  public <T> boolean contains(Flag<T> flag) {
    return flags.containsKey(flag.name());
  }
}
