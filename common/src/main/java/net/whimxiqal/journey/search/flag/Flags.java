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

package net.whimxiqal.journey.search.flag;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.util.TimeUtil;

public final class Flags {

  public static final Flag<Integer> TIMEOUT = Flag.of("timeout",
      (Function<Integer, String>) TimeUtil::toSimpleTime,
      Settings.DEFAULT_SEARCH_TIMEOUT::getValue,
      Integer.class);
  public static final Flag<Integer> ANIMATE = Flag.of("animate",
      (Function<Integer, String>) TimeUtil::toSimpleTimeMilliseconds,
      () -> 0,
      Integer.class);
  public static final Flag<Boolean> DIG = Flag.of("dig", Object::toString, Settings.DEFAULT_DIG_FLAG::getValue, Boolean.class);
  public static final Flag<Boolean> DOOR = Flag.of("door", Object::toString, Settings.DEFAULT_DOORS_FLAG::getValue, Boolean.class);
  public static final Flag<Boolean> FLY = Flag.of("fly", Object::toString, Settings.DEFAULT_FLY_FLAG::getValue, Boolean.class);

  public static final List<Flag<?>> allFlags = new LinkedList<>();

  static {
    Arrays.stream(Flags.class.getDeclaredFields())
        .map(field -> {
          try {
            return field.get(null);
          } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
          }
        })
        .filter(object -> object instanceof Flag<?>)
        .map(object -> ((Flag<?>) object))
        .forEach(allFlags::add);
  }

  private Flags() {
  }

}
