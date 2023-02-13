package net.whimxiqal.journey;

import java.util.Collection;

/**
 * A supplier that gives a collection of tunnels when given a player.
 */
@FunctionalInterface
public interface TunnelSupplier {

  /**
   * Get a collection of tunnels when given a specific player who's requesting them.
   *
   * @param player the player
   * @return the tunnels
   */
  Collection<? extends Tunnel> tunnels(JourneyPlayer player);

}
