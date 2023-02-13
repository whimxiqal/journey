package net.whimxiqal.journey;

import java.util.NoSuchElementException;

/**
 * The static provider of a {@link JourneyApi}.
 */
public final class JourneyApiProvider {

  private static JourneyApi instance;

  private JourneyApiProvider() {
  }

  /**
   * Getter for the {@link JourneyApi}.
   *
   * @return the Journey API
   */
  public static JourneyApi get() {
    if (instance == null) {
      throw new NoSuchElementException("No JourneyApi has been set yet.");
    }
    return instance;
  }

  static void provide(JourneyApi instance) {
    JourneyApiProvider.instance = instance;
  }

}
