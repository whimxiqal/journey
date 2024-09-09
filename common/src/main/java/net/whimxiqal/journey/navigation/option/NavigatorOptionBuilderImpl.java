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

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A builder for a {@link NavigatorOption}.
 *
 * @param <T> the type of stored value
 */
public class NavigatorOptionBuilderImpl<T> implements NavigatorOptionBuilder<T> {

  private final String optionId;
  private final Class<T> clazz;
  private Supplier<T> defaultValueSupplier;
  private NavigatorOptionParser<T> parser;
  private Supplier<List<String>> valueSuggestions;
  private NavigatorOptionValidator<T> validator;
  private String permission;
  private Function<T, String> valuePermissionFunction;

  NavigatorOptionBuilderImpl(String optionId, Class<T> clazz) {
    this.optionId = optionId;
    this.clazz = clazz;
  }

  @Override
  public NavigatorOptionBuilder<T> parser(NavigatorOptionParser<T> parser) {
    this.parser = parser;
    return this;
  }

  @Override
  public NavigatorOptionBuilder<T> defaultValue(Supplier<T> defaultValueSupplier) {
    this.defaultValueSupplier = defaultValueSupplier;
    return this;
  }

  @Override
  public NavigatorOptionBuilder<T> defaultValue(T defaultValue) {
    this.defaultValueSupplier = () -> defaultValue;
    return this;
  }

  @Override
  public NavigatorOptionBuilder<T> valueSuggestions(Supplier<List<String>> valueSuggestions) {
    this.valueSuggestions = valueSuggestions;
    return this;
  }

  @Override
  public NavigatorOptionBuilder<T> validator(NavigatorOptionValidator<T> validator) {
    this.validator = validator;
    return this;
  }

  @Override
  public NavigatorOptionBuilder<T> permission(String permission) {
    this.permission = permission;
    return this;
  }

  @Override
  public NavigatorOptionBuilder<T> valuePermission(Function<T, String> valuePermissionFunction) {
    this.valuePermissionFunction = valuePermissionFunction;
    return this;
  }

  @Override
  public NavigatorOption<T> build() {
    return new NavigatorOptionImpl<>(optionId, clazz,
        defaultValueSupplier, parser,
        valueSuggestions, validator,
        permission, valuePermissionFunction);
  }
}
