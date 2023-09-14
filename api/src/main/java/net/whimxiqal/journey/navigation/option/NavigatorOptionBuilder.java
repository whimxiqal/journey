package net.whimxiqal.journey.navigation.option;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import net.whimxiqal.journey.Builder;
import net.whimxiqal.journey.JourneyAgent;

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

  public NavigatorOptionBuilder<T> parser(NavigatorOptionParser<T> parser) {
    this.parser = parser;
    return this;
  }

  public NavigatorOptionBuilder<T> defaultValue(Supplier<T> defaultValueSupplier) {
    this.defaultValueSupplier = defaultValueSupplier;
    return this;
  }

  public NavigatorOptionBuilder<T> defaultValue(T defaultValue) {
    this.defaultValueSupplier = () -> defaultValue;
    return this;
  }

  public NavigatorOptionBuilder<T> valueSuggestions(Supplier<List<String>> valueSuggestions) {
    this.valueSuggestions = valueSuggestions;
    return this;
  }

  public NavigatorOptionBuilder<T> validator(NavigatorOptionValidator<T> validator) {
    this.validator = validator;
    return this;
  }

  public NavigatorOptionBuilder<T> permission(String permission) {
    this.permission = permission;
    return this;
  }

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
