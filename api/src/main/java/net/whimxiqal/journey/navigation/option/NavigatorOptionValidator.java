package net.whimxiqal.journey.navigation.option;

import org.jetbrains.annotations.Nullable;

/**
 * A functional interface for a validator of values for a {@link NavigatorOption}.
 *
 * @param <T> the type of value to validate
 */
@FunctionalInterface
public interface NavigatorOptionValidator<T> {

  /**
   * Validate the value by returning the resultant error, or null if there was no error.
   *
   * @param value the value to check
   * @return the error, or null if none
   */
  @Nullable String validate(T value);

}
