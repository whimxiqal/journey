package net.whimxiqal.journey.navigation;

import java.util.HashMap;
import java.util.Map;
import net.whimxiqal.journey.navigation.option.NavigatorOption;
import net.whimxiqal.journey.navigation.option.NavigatorOptionValues;

public class NavigatorOptionValuesImpl implements NavigatorOptionValues {

  private final Map<String, NavigatorOption<?>> options;
  private final Map<String, Object> serializedOptionValues;

  public NavigatorOptionValuesImpl(Map<String, NavigatorOption<?>> options, Map<String, Object> serializedOptionValues) {
    this.options = new HashMap<>(options);  // copy
    this.serializedOptionValues = serializedOptionValues;
  }

  @Override
  public <T> T value(NavigatorOption<T> option) throws IllegalArgumentException {
    if (serializedOptionValues == null) {
      return option.getDefault();
    }
    Object value = serializedOptionValues.get(option.optionId());
    if (value == null) {
      return option.getDefault();
    }
    if (!option.getValueClass().isInstance(value)) {
      throw new IllegalArgumentException("Serialized options has invalid value type for option " + option.optionId());
    }
    return (T) value;
  }

  @Override
  public <T> T value(String optionId, Class<T> type) throws IllegalArgumentException {
    NavigatorOption<?> option = options.get(optionId);
    if (option == null) {
      throw new IllegalArgumentException("No option found with id " + optionId);
    }
    Object value = value(option);
    if (!type.isInstance(value)) {
      throw new IllegalArgumentException("The value stored for option " + optionId
          + " is not of requested type " + type.getCanonicalName());
    }
    return (T) value;
  }

  public <T> void addOption(NavigatorOption<T> option) {
    if (this.options.containsKey(option.optionId())) {
      throw new IllegalArgumentException("Tried to register an option with id " + option.optionId() + " but one already existed");
    }
    this.options.put(option.optionId(), option);
  }
}
