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

public class NavigatorOptionBuilderFactoryImpl implements NavigatorOptionBuilderFactory {

  @Override
  public <X> NavigatorOptionBuilder<X> builder(String optionId, Class<X> clazz) {
    return new NavigatorOptionBuilderImpl<>(optionId, clazz);

  }

  @Override
  public NavigatorOptionBuilder<String> stringValueBuilder(String optionId) {
    return new NavigatorOptionBuilderImpl<>(optionId, String.class).parser(s -> s);
  }

  @Override
  public NavigatorOptionBuilder<Integer> integerValueBuilder(String optionId, int min, int max) {
    return new NavigatorOptionBuilderImpl<>(optionId, Integer.class)
        .parser(val -> {
          try {
            return Integer.parseInt(val);
          } catch (NumberFormatException e) {
            throw new ParseNavigatorOptionException(e.getMessage(), 0);
          }
        })
        .validator(val -> {
          if (val < min) {
            return "Value must be greater than " + min;
          } else if (val > max) {
            return "Value must be less than " + max;
          }
          return null;
        });
  }

  @Override
  public NavigatorOptionBuilder<Double> doubleValueBuilder(String optionId, double min, double max) {
    return new NavigatorOptionBuilderImpl<>(optionId, Double.class)
        .parser(Double::parseDouble)
        .validator(val -> {
          if (val < min) {
            return "Value must be greater than " + min;
          } else if (val > max) {
            return "Value must be less than " + max;
          }
          return null;
        });
  }

}
