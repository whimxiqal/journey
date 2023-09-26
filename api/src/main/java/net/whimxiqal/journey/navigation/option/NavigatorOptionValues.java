package net.whimxiqal.journey.navigation.option;

import java.util.NoSuchElementException;

/**
 * A supplier of values for option values given {@link NavigatorOption}s.
 */
public interface NavigatorOptionValues {

  /**
   * Supplies the value for the {@link NavigatorOption}.
   * The supplied value might be the option's default value if the underlying struct
   * doesn't have any value specified.
   *
   * @param option the option
   * @param <T>    the type of the value
   * @return the value
   */
  <T> T value(NavigatorOption<T> option);

  /**
   * Supplies the value for the {@link NavigatorOption} with the given id.
   * The supplied value might be the option's default value if the underlying struct
   * doesn't have any value specified.
   * When calling this method, make sure you have the right value type of the target option.
   *
   * @param optionId the option id
   * @param type     the type of the desired value
   * @param <T>      the type of the value
   * @return the value
   * @throws NoSuchElementException if no option is found with given id
   */
  <T> T value(String optionId, Class<T> type) throws NoSuchElementException;

  /**
   * Supplies the value for the {@link NavigatorOption} with the given id.
   * The supplied value might be the option's default value if the underlying struct
   * doesn't have any value specified.
   *
   * @param optionId the option id
   * @return the value
   * @throws NoSuchElementException if no option is found with given id
   */
  Object value(String optionId) throws NoSuchElementException;

}
