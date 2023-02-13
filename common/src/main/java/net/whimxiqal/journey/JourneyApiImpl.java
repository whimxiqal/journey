package net.whimxiqal.journey;

public class JourneyApiImpl implements JourneyApi {

  @Override
  public void registerScope(String plugin, String id, Scope scope) {
    if (!Journey.get().proxy().platform().synchronous()) {
      Journey.logger().warn("Plugin " + plugin + " tried to register a scope asynchronously. Please contact the plugin owner.");
      return;
    }
    Journey.get().scopeManager().register(plugin, id, scope);
  }

  @Override
  public void registerTunnels(String plugin, TunnelSupplier tunnelSupplier) {
    if (!Journey.get().proxy().platform().synchronous()) {
      Journey.logger().warn("Plugin " + plugin + " tried to register a tunnels asynchronously. Please contact the plugin owner.");
      return;
    }
    Journey.get().tunnelManager().register(tunnelSupplier);
  }
}
