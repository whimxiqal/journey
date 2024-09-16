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

import java.util.Map;
import java.util.Optional;
import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.navigation.option.NavigatorOption;
import net.whimxiqal.journey.navigation.option.NavigatorOptionValues;
import org.jetbrains.annotations.Nullable;

class NavigatorFactoryImpl implements NavigatorFactory {

  private final String plugin;
  private final String navigatorType;
  private final String permission;
  private final Map<String, NavigatorOption<?>> options;
  private final NavigatorSupplier navigatorSupplier;

  NavigatorFactoryImpl(String plugin, String navigatorType, @Nullable String permission,
                       Map<String, NavigatorOption<?>> options, NavigatorSupplier navigatorSupplier) {
    this.plugin = plugin;
    this.navigatorType = navigatorType;
    this.permission = permission;
    this.options = options;
    this.navigatorSupplier = navigatorSupplier;
  }

  @Override
  public String plugin() {
    return plugin;
  }

  @Override
  public String navigatorType() {
    return navigatorType;
  }

  @Override
  public Optional<String> permission() {
    return Optional.ofNullable(permission);
  }

  @Override
  public Map<String, NavigatorOption<?>> options() {
    return options;
  }

  @Override
  public Navigator navigator(JourneyAgent agent,
                             NavigationProgress progress,
                             NavigatorOptionValues optionValues) {
    return navigatorSupplier.navigator(agent, progress, optionValues);
  }
}
