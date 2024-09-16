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

package net.whimxiqal.journey.navigation.option;

import java.util.NoSuchElementException;

/**
 * A supplier of values for option values given {@link NavigatorOption}s.
 */
public interface NavigatorOptionValues {

  /**
   * Supplies the value for the {@link NavigatorOption}.
   * The supplied value might be the option's default value if the underlying struct
   * doesn't have any value specified.
   *
   * @param option the option
   * @param <T>    the type of the value
   * @return the value
   */
  <T> T value(NavigatorOption<T> option);

  /**
   * Supplies the value for the {@link NavigatorOption} with the given id.
   * The supplied value might be the option's default value if the underlying struct
   * doesn't have any value specified.
   * When calling this method, make sure you have the right value type of the target option.
   *
   * @param optionId the option id
   * @param type     the type of the desired value
   * @param <T>      the type of the value
   * @return the value
   * @throws NoSuchElementException if no option is found with given id
   */
  <T> T value(String optionId, Class<T> type) throws NoSuchElementException;

  /**
   * Supplies the value for the {@link NavigatorOption} with the given id.
   * The supplied value might be the option's default value if the underlying struct
   * doesn't have any value specified.
   *
   * @param optionId the option id
   * @return the value
   * @throws NoSuchElementException if no option is found with given id
   */
  Object value(String optionId) throws NoSuchElementException;

}
