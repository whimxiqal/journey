package net.whimxiqal.journey.navigation.option;

/**
 * A function interface for parsing a value for a {@link NavigatorOption}.
 *
 * @param <T> the type of value to parse
 */
@FunctionalInterface
public interface NavigatorOptionParser<T> {

  /**
   * Parse the option value from its serialized string.
   *
   * @param value the serialized value
   * @return the value
   * @throws ParseNavigatorOptionException thrown when an error occurs trying to parse
   */
  T parse(String value) throws ParseNavigatorOptionException;

}
