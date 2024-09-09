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

import java.util.HashMap;
import java.util.Map;
import net.whimxiqal.journey.navigation.option.NavigatorOption;

public class NavigatorFactoryBuilderImpl implements NavigatorFactoryBuilder {

  private final String plugin;
  private final String navigatorType;
  private final Map<String, NavigatorOption<?>> options = new HashMap<>();
  private String permission;
  private NavigatorSupplier navigatorSupplier;

  NavigatorFactoryBuilderImpl(String plugin, String navigatorType) {
    this.plugin = plugin;
    this.navigatorType = navigatorType;
  }

  @Override
  public NavigatorFactoryBuilder permission(String permission) {
    this.permission = permission;
    return this;
  }

  @Override
  public <T> NavigatorFactoryBuilder option(NavigatorOption<T> option) {
    options.put(option.optionId(), option);
    return this;
  }

  @Override
  public NavigatorFactoryBuilder supplier(NavigatorSupplier supplier) {
    this.navigatorSupplier = supplier;
    return this;
  }

  @Override
  public NavigatorFactory build() {
    if (navigatorSupplier == null) {
      throw new IllegalStateException("Navigator factories must have a navigator supplier");
    }
    return new NavigatorFactoryImpl(plugin, navigatorType, permission, options, navigatorSupplier);
  }
}
