package net.whimxiqal.journey;

/**
 * The central interface for all external-facing endpoints for Journey.
 */
public interface JourneyApi {

  /**
   * Register a {@link Scope} to Journey. These will appear throughout the plugin,
   * allowing players to journey to custom locations.
   *
   * @param id    the id of the scope. Characters must be alphanumeric, a hyphen, or a space.
   * @param scope the scope
   */
  void registerScope(String plugin, String id, Scope scope);

  /**
   * Register a supplier of {@link Tunnel}s, indicating unique methods of travel through which
   * players may get to locations unnaturally.
   *
   * @param tunnelSupplier the supplier of tunnels for a given player
   */
  void registerTunnels(String plugin, TunnelSupplier tunnelSupplier);

}
