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

package net.whimxiqal.journey.navigation;

import java.util.Collections;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * A struct for holding serialized information about a new navigator.
 *
 * @param navigatorType the type of navigator
 * @param options       the options to manipulate the behavior of the navigator
 */
public record NavigatorDetails(String navigatorType,
                               Map<String, Object> options) implements Comparable<NavigatorDetails> {

  /**
   * Static constructor with default options.
   *
   * @param navigatorType navigator type
   * @return the details
   */
  public static NavigatorDetails of(String navigatorType) {
    return new NavigatorDetails(navigatorType, Collections.emptyMap());
  }

  @Override
  public int compareTo(@NotNull NavigatorDetails o) {
    return navigatorType.compareTo(o.navigatorType);
  }
}
