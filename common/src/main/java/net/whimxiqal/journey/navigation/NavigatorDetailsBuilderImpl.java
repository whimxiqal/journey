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
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.navigation.option.NavigatorOption;

public abstract class NavigatorDetailsBuilderImpl<B extends NavigatorDetailsBuilder<B>> implements NavigatorDetailsBuilder<B> {

  private final String navigatorType;
  protected final Map<String, Object> options = new HashMap<>();

  protected NavigatorDetailsBuilderImpl(String navigatorType) {
    this.navigatorType = navigatorType;
  }

  @Override
  public NavigatorDetails build() {
    return new NavigatorDetails(navigatorType, options);
  }

  @Override
  public B setOption(String key, Object value) {
    options.put(key, value);
    return getDerived();
  }

  @Override
  public <T> B setOption(NavigatorOption<T> option, T value) {
    options.put(option.optionId(), value);
    return getDerived();
  }

  @Override
  public B completionMessage(Component message) {
    options.put(NavigationManager.NAVIGATOR_OPTION_ID_COMPLETION_MESSAGE, message);
    return getDerived();
  }

  @Override
  public B completionTitle(Component title) {
    options.put(NavigationManager.NAVIGATOR_OPTION_ID_COMPLETION_TITLE, title);
    return getDerived();
  }

  @Override
  public B completionSubtitle(Component subtitle) {
    options.put(NavigationManager.NAVIGATOR_OPTION_ID_COMPLETION_SUBTITLE, subtitle);
    return getDerived();
  }

  protected abstract B getDerived();

  public static class Self extends NavigatorDetailsBuilderImpl<Self> {
    public Self(String navigatorType) {
      super(navigatorType);
    }

    @Override
    protected Self getDerived() {
      return this;
    }

  }
}
