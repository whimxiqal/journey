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

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.navigation.NavigatorDetails;
import net.whimxiqal.journey.util.Permission;

public final class Flags {

  public static final IntegerFlag TIMEOUT = new SecondsFlag("timeout",
      Settings.DEFAULT_SEARCH_TIMEOUT::getValue,
      Permission.FLAG_TIMEOUT.path(),
      0, 24 * 60 * 60 /* a day */,
      0, 5, 10, 30, 60,
      2 * 60 /* two minutes */,
      5 * 60 /* five minutes */);
  public static final Flag<Integer> ANIMATE = new MillisecondsFlag("animate", () -> 0,
      Permission.FLAG_ANIMATE.path(),
      0, 30000 /* 30 seconds, there's no reason this has to be this high */,
      0, 5, 20, 100, 1000 /* 1 second */, 10000 /* 10 seconds */);
  public static final Flag<Boolean> DIG = new BooleanFlag("dig",
      Settings.DEFAULT_DIG_FLAG::getValue,
      Permission.FLAG_DIG.path());
  public static final Flag<Boolean> DOOR = new BooleanFlag("door",
      Settings.DEFAULT_DOORS_FLAG::getValue,
      Permission.FLAG_DOOR.path());
  public static final Flag<Boolean> FLY = new BooleanFlag("fly",
      Settings.DEFAULT_FLY_FLAG::getValue,
      Permission.FLAG_FLY.path());
  public static final Flag<NavigatorDetails> NAVIGATOR = new NavigatorDetailsFlag("navigator",
      () -> NavigatorDetails.of(Settings.DEFAULT_NAVIGATOR.getValue()),
      Permission.FLAG_NAVIGATOR.path());
  public static final List<Flag<?>> ALL_FLAGS = new LinkedList<>();

  static {
    for (Field field : Flags.class.getDeclaredFields()) {
      Object obj;
      try {
        obj = field.get(null);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
        continue;
      }
      if (!(obj instanceof Flag<?> flag)) {
        continue;
      }
      ALL_FLAGS.add(flag);
    }
  }

  private Flags() {
  }

}
