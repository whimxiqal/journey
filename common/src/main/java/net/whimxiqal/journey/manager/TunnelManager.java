package net.whimxiqal.journey.manager;

import java.util.LinkedList;
import java.util.List;
import net.whimxiqal.journey.JourneyPlayer;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.TunnelSupplier;
import net.whimxiqal.journey.scope.ScopeUtil;

public class TunnelManager {

  private final List<TunnelSupplier> tunnelSuppliers = new LinkedList<>();

  public void register(TunnelSupplier tunnelSupplier) {
    tunnelSuppliers.add(tunnelSupplier);
  }

  public List<Tunnel> tunnels(JourneyPlayer player) {
    List<Tunnel> tunnels = new LinkedList<>();
    for (TunnelSupplier supplier : tunnelSuppliers) {
      for (Tunnel tunnel : supplier.tunnels(player)) {
        if (tunnel.permission().isEmpty() || player.hasPermission(tunnel.permission().get())) {
          tunnels.add(tunnel);
        }
      }
    }
    return tunnels;
  }
}
