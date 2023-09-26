package net.whimxiqal.journey.navigation;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import net.whimxiqal.journey.navigation.option.NavigatorOption;
import net.whimxiqal.journey.navigation.option.NavigatorOptionParser;
import net.whimxiqal.journey.navigation.option.NavigatorOptionValues;
import net.whimxiqal.journey.navigation.option.ParseNavigatorOptionException;

public class NavigatorOptionValuesImpl implements NavigatorOptionValues {

  private final Map<String, NavigatorOption<?>> options;
  private final Map<String, Object> serializedOptionValues;

  public NavigatorOptionValuesImpl(Map<String, NavigatorOption<?>> options, Map<String, Object> serializedOptionValues) {
    this.options = new HashMap<>(options);  // copy
    this.serializedOptionValues = serializedOptionValues;

    // add defaults
    addOption(NavigationManager.COMPLETION_MESSAGE_OPTION);
    addOption(NavigationManager.COMPLETION_TITLE_OPTION);
    addOption(NavigationManager.COMPLETION_SUBTITLE_OPTION);
  }

  @Override
  public <T> T value(NavigatorOption<T> option) {
    if (serializedOptionValues == null) {
      return option.getDefault();
    }
    Object value = serializedOptionValues.get(option.optionId());
    if (value == null) {
      return option.getDefault();
    }
    if (option.getValueClass().isInstance(value)) {
      return (T) value;
    }
    Optional<NavigatorOptionParser<T>> parser = option.parser();
    if (!(value instanceof String)) {
      throw new ClassCastException("Value for option id " + option.optionId()
          + " was of type " + value.getClass().getName()
          + ", not " + option.getValueClass().getName() + " or " + String.class.getName());
    }
    if (parser.isEmpty()) {
      throw new IllegalStateException("Value for option id " + option.optionId()
          + " is a string, but there is no parser assigned to the option");
    }
    T parsed;
    try {
      parsed = parser.get().parse((String) value);
    } catch (ParseNavigatorOptionException e) {
      throw new IllegalArgumentException(e);
    }
    String validationError = option.validate(parsed);
    if (validationError != null) {
      throw new IllegalStateException(validationError);
    }
    return parsed;
  }

  @Override
  public <T> T value(String optionId, Class<T> type) throws NoSuchElementException, ClassCastException {
    return type.cast(value(optionId));
  }

  @Override
  public Object value(String optionId) throws NoSuchElementException {
    NavigatorOption<?> option = options.get(optionId);
    if (option == null) {
      throw new NoSuchElementException("No option found with id " + optionId);
    }
    return serializedOptionValues.getOrDefault(optionId, option.getDefault());
  }

  private <T> void addOption(NavigatorOption<T> option) {
    this.options.put(option.optionId(), option);
  }
}
