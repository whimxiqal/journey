package net.whimxiqal.journey;

/**
 * An object that ultimately just builds another one.
 *
 * @param <T> the object to build
 */
public interface Builder<T> {

  /**
   * Build the object.
   *
   * @return the built object
   */
  T build();

}
