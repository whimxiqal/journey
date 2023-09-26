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
