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
import net.whimxiqal.journey.Builder;

/**
 * A builder for a {@link NavigatorOption}.
 *
 * @param <T> the type of stored value
 */
public class NavigatorOptionBuilder<T> implements Builder<NavigatorOption<T>> {

  private final String optionId;
  private final Class<T> clazz;
  private Supplier<T> defaultValueSupplier;
  private NavigatorOptionParser<T> parser;
  private Supplier<List<String>> valueSuggestions;
  private NavigatorOptionValidator<T> validator;
  private String permission;
  private Function<T, String> valuePermissionFunction;

  NavigatorOptionBuilder(String optionId, Class<T> clazz) {
    this.optionId = optionId;
    this.clazz = clazz;
  }

  /**
   * Set the parser, thereby enabling parsing of the option. Once the parser is set,
   * players in game may specify the option in commands and other API users may provide
   * the value as a {@link String} when starting navigation.
   *
   * @param parser the parser
   * @return the builder, for chaining
   */
  public NavigatorOptionBuilder<T> parser(NavigatorOptionParser<T> parser) {
    this.parser = parser;
    return this;
  }

  /**
   * Set a supplier for the default value.
   *
   * @param defaultValueSupplier the supplier
   * @return the builder, for chaining
   */
  public NavigatorOptionBuilder<T> defaultValue(Supplier<T> defaultValueSupplier) {
    this.defaultValueSupplier = defaultValueSupplier;
    return this;
  }

  /**
   * Set a default value.
   *
   * @param defaultValue the value
   * @return the builder, for chaining
   */
  public NavigatorOptionBuilder<T> defaultValue(T defaultValue) {
    this.defaultValueSupplier = () -> defaultValue;
    return this;
  }

  /**
   * Set a supplier for a list of suggestions for this value.
   *
   * @param valueSuggestions the suggestions
   * @return the builder, for chaining
   */
  public NavigatorOptionBuilder<T> valueSuggestions(Supplier<List<String>> valueSuggestions) {
    this.valueSuggestions = valueSuggestions;
    return this;
  }

  /**
   * Set a {@link NavigatorOptionValidator} for the option. The validator should return an error string
   * if there is an issue with the value.
   * The validator is run regardless of whether the value was parsed from a string.
   *
   * @param validator the validator
   * @return the builder, for chaining
   */
  public NavigatorOptionBuilder<T> validator(NavigatorOptionValidator<T> validator) {
    this.validator = validator;
    return this;
  }

  /**
   * Set a permission for this option. Only players with this permission may use this option.
   *
   * @param permission the permission
   * @return the builder, for chaining
   */
  public NavigatorOptionBuilder<T> permission(String permission) {
    this.permission = permission;
    return this;
  }

  /**
   * Set a function for retrieving a permission based on a given value.
   * Only players with the given permission may use the option with the given value.
   *
   * @param valuePermissionFunction the permission function
   * @return the builder, for chaining
   */
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
