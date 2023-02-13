package net.whimxiqal.journey;

/**
 * A connection from one location to another, indicating the ability for a player to travel through
 * unnatural methods like by typing commands to teleport.
 */
public interface Tunnel extends Permissible {

  int DEFAULT_COST = 1;

  static TunnelBuilder builder(Cell origin, Cell destination) {
    return new TunnelBuilder(origin, destination);
  }

  /**
   * The starting location, or "entrance" to the tunnel.
   *
   * @return the origin
   */
  Cell origin();

  /**
   * The ending location, or "exit" of the tunnel.
   *
   * @return the destination
   */
  Cell destination();

  /**
   * The cost of traveling along the tunnel. A cost of 1 indicates the cost to walk a single block.
   * This may not be negative.
   *
   * @return the cost
   */
  default int cost() {
    return DEFAULT_COST;
  }

  /**
   * The prompt to signal that the tunnel should be traversed.
   * This is useful when the player needs guidance on how to enter the tunnel,
   * like which command they need to type.
   */
  default void prompt() {
    // nothing
  }

  /**
   * Whether the given location is one which constitutes the tunnel being traversed.
   * By default, the tunnel is completed if the distance from the destination is at most 1.
   *
   * @param location the location
   * @return true if the tunnel would be completed with the given location
   */
  default boolean testCompletion(Cell location) {
    return location.distanceToSquared(destination()) <= 1;
  }

}
