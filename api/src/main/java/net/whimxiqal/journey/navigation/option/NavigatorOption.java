package net.whimxiqal.journey.navigation.option;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.Permissible;
import org.jetbrains.annotations.Nullable;

public interface NavigatorOption<T> {

  // TODO make these builders, not static factories, since we now have quite a list of parameters
  static <X> NavigatorOptionBuilder<X> builder(String optionsId, Class<X> clazz) {
    return new NavigatorOptionBuilder<>(optionsId, clazz);
  }

  static NavigatorOptionBuilder<String> stringValueBuilder(String optionId) {
    return new NavigatorOptionBuilder<>(optionId, String.class).parser(s -> s);
  }

  static NavigatorOptionBuilder<Integer> integerValueBuilder(String optionId, int min, int max) {
    return new NavigatorOptionBuilder<>(optionId, Integer.class)
        .parser(val -> {
          try {
            return Integer.parseInt(val);
          } catch (NumberFormatException e) {
            throw new ParseNavigatorOptionException("", 0);
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

  static NavigatorOptionBuilder<Double> doubleValueBuilder(String optionId, double min, double max) {
    return new NavigatorOptionBuilder<>(optionId, Double.class)
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

  String optionId();

  List<String> valueSuggestions();

  boolean canParse();

  T parse(String value) throws ParseNavigatorOptionException;

  String validate(T value);

  T getDefault();

  Optional<String> permission();

  Optional<String> valuePermission(T value);

  Class<T> getValueClass();

}
