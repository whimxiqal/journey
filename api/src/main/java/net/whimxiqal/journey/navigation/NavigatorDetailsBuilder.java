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

import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.Builder;
import net.whimxiqal.journey.navigation.option.NavigatorOption;

/**
 * A builder for {@link NavigatorDetails}.
 *
 * @param <B> the derived type of builder, for more helpful chaining
 */
public interface NavigatorDetailsBuilder<B extends NavigatorDetailsBuilder<B>>
    extends Builder<NavigatorDetails> {

  /**
   * Set a new option value.
   *
   * @param key   the option id
   * @param value the option value
   * @return the builder, for chaining
   */
  B setOption(String key, Object value);

  /**
   * Set a new option value using the option and type safety.
   *
   * @param option the option
   * @param value  the value
   * @param <T>    the type of the option's value
   * @return the builder, for chaining
   */
  <T> B setOption(NavigatorOption<T> option, T value);

  /**
   * Set the completion message, which will be sent to the agent upon completion of the navigator.
   *
   * @param message the message
   * @return the builder, for chaining
   */
  B completionMessage(Component message);

  /**
   * Set the completion title, which will be sent to the agent upon completion of the navigator.
   *
   * @param title the title
   * @return the builder, for chaining
   */
  B completionTitle(Component title);

  /**
   * Set the completion subtitle, which will be sent to the agent upon completion of the navigator.
   *
   * @param subtitle the subtitle
   * @return the builder, for chaining
   */
  B completionSubtitle(Component subtitle);

}
