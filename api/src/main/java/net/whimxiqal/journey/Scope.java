package net.whimxiqal.journey;

/**
 * A nested collection of destinations.
 */
public interface Scope extends Describable, Permissible {

  /**
   * A builder for a {@link Scope}.
   *
   * @return the builder
   */
  static ScopeBuilder builder() {
    return new ScopeBuilder();
  }

  /**
   * A supplier of scopes to be accessed only under the current scope.
   *
   * @param player the player
   * @return the supplier of sub-scopes
   */
  default VirtualMap<Scope> subScopes(JourneyPlayer player) {
    return VirtualMap.empty();
  }

  /**
   * A supplier of destinations to which players may travel.
   *
   * @param player the player
   * @return the supplier
   */
  default VirtualMap<Destination> destinations(JourneyPlayer player) {
    return VirtualMap.empty();
  }

  /**
   * Whether this scope's ID must be specified to properly
   * contextualize this scope's destinations and sub-scopes.
   *
   * <p>This should be true if players generally don't need/want to know
   * about the contents of this scope, but it's still available to them if
   * they go looking for it by specifying this scope
   *
   * @return true if the contents of this scope should only ever been referenced strictly under this scope
   */
  default boolean isStrict() {
    return false;
  }

}
