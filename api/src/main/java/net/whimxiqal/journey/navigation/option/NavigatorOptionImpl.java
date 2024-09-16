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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

class NavigatorOptionImpl<T> implements NavigatorOption<T> {

  private final String optionId;
  private final Class<T> clazz;
  private final Supplier<T> defaultValueSupplier;
  private final NavigatorOptionParser<T> parser;
  private final Supplier<List<String>> valueSuggestions;
  private final NavigatorOptionValidator<T> validator;
  private final String permission;
  private final Function<T, String> valuePermissionFunction;

  NavigatorOptionImpl(String optionId, Class<T> clazz,
                      Supplier<T> defaultValueSupplier,
                      @Nullable NavigatorOptionParser<T> parser,
                      @Nullable Supplier<List<String>> valueSuggestions,
                      @Nullable NavigatorOptionValidator<T> validator,
                      String permission,
                      Function<T, String> valuePermissionFunction) {
    this.optionId = optionId;
    this.clazz = clazz;
    if (defaultValueSupplier == null) {
      throw new IllegalArgumentException("The default value supplier may not be null");
    }
    this.defaultValueSupplier = defaultValueSupplier;
    this.parser = parser;
    this.valueSuggestions = valueSuggestions == null ? Collections::emptyList : valueSuggestions;
    this.validator = validator == null ? val -> null : validator;
    this.permission = permission;
    this.valuePermissionFunction = valuePermissionFunction;
  }

  @Override
  public String optionId() {
    return optionId;
  }

  @Override
  public List<String> valueSuggestions() {
    return valueSuggestions.get();
  }

  @Override
  public Optional<NavigatorOptionParser<T>> parser() {
    return Optional.ofNullable(parser);
  }

  @Override
  public String validate(T value) {
    return validator.validate(value);
  }

  @Override
  public T getDefault() {
    return defaultValueSupplier.get();
  }

  @Override
  public Optional<String> permission() {
    return Optional.ofNullable(permission);
  }

  @Override
  public Optional<String> valuePermission(T value) {
    return Optional.ofNullable(valuePermissionFunction).map(func -> func.apply(value));
  }

  @Override
  public Class<T> getValueClass() {
    return clazz;
  }

}
