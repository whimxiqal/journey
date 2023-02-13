package net.whimxiqal.journey.bukkit;

import java.util.NoSuchElementException;

/**
 * The static provider of a {@link JourneyBukkitApi}.
 */
public final class JourneyBukkitApiProvider {

  private static JourneyBukkitApi instance;

  /**
   * Getter for the {@link JourneyBukkitApi}.
   *
   * @return the Journey Bukkit API
   */
  public static JourneyBukkitApi get() {
    if (instance == null) {
      throw new NoSuchElementException("No JourneyBukkitApi has been set yet.");
    }
    return instance;
  }

  static void provide(JourneyBukkitApi instance) {
    JourneyBukkitApiProvider.instance = instance;
  }

  private JourneyBukkitApiProvider() {
  }

}
