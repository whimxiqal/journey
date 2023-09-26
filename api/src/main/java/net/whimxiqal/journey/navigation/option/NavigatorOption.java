package net.whimxiqal.journey.navigation.option;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * An option for a navigator, which will manipulate the behavior of its owning navigator.
 *
 * @param <T> the type of stored value
 */
public interface NavigatorOption<T> extends NavigatorOptionValidator<T> {

  /**
   * Static constructor for a generic builder of a {@link NavigatorOption}.
   *
   * @param optionId the id the option
   * @param clazz    the class of the type of stored value
   * @param <X>      the type of stored value
   * @return the builder
   */
  static <X> NavigatorOptionBuilder<X> builder(String optionId, Class<X> clazz) {
    return new NavigatorOptionBuilder<>(optionId, clazz);
  }

  /**
   * Static constructor for an option wrapping a string value with a default parser already specified.
   *
   * @param optionId the id of the option
   * @return the builder
   */
  static NavigatorOptionBuilder<String> stringValueBuilder(String optionId) {
    return new NavigatorOptionBuilder<>(optionId, String.class).parser(s -> s);
  }

  /**
   * Static constructor for an option wrapping an integer value with a default parser and validator
   * already specified.
   *
   * @param optionId the id of the option
   * @param min      the minimum allowed value
   * @param max      the maximum allowed value
   * @return the builder
   */
  static NavigatorOptionBuilder<Integer> integerValueBuilder(String optionId, int min, int max) {
    return new NavigatorOptionBuilder<>(optionId, Integer.class)
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

  /**
   * Static constructor for an option wrapping a double value with a default parser and validator
   * already specified.
   *
   * @param optionId the id of the option
   * @param min      the minimum allowed value
   * @param max      the maximum allowed value
   * @return the builder
   */
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

  /**
   * The id of the option. This must be unique to this option's owning
   * {@link net.whimxiqal.journey.navigation.Navigator}.
   *
   * @return the option id
   */
  String optionId();

  /**
   * Suggested values for this option.
   *
   * @return a list of suggestions, or empty if none
   */
  default List<String> valueSuggestions() {
    return Collections.emptyList();
  }

  /**
   * The default value for this option.
   *
   * @return the default
   */
  T getDefault();

  /**
   * Get a parser for parsing option values from strings.
   * Returning empty signals that players may not parse values for this option,
   * and can therefore not use this option in game.
   * Also, other users of the API who rely on serializing navigator option values
   * must deserialize directly to the option type {@link T} themselves.
   *
   * @return the parser
   */
  default Optional<NavigatorOptionParser<T>> parser() {
    return Optional.empty();
  }

  /**
   * The permission required to use this option in game.
   *
   * @return the permission, or empty if anyone can use it
   */
  default Optional<String> permission() {
    return Optional.empty();
  }

  /**
   * The permission required to use the given value in game.
   *
   * @param value the value in question
   * @return the permission, or empty if anyone can use it
   */
  default Optional<String> valuePermission(T value) {
    return Optional.empty();
  }

  /**
   * The class object for the type of the stored value {@link T}.
   *
   * @return the class object
   */
  Class<T> getValueClass();

}
