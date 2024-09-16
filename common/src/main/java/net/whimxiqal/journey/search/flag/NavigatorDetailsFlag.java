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

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.navigation.NavigatorDetails;

public class NavigatorDetailsFlag extends Flag<NavigatorDetails> {
  public NavigatorDetailsFlag(String name, Supplier<NavigatorDetails> defaultValue, String permission) {
    super(name, defaultValue, permission, NavigatorDetails.class);
  }

  @Override
  public String printValue(NavigatorDetails val) {
    return val.navigatorType();
  }

  @Override
  public List<NavigatorDetails> suggestedValues() {
    return Journey.get().navigatorManager().navigators()
        .stream()
        .map(NavigatorDetails::of)
        .collect(Collectors.toList());
  }
}
