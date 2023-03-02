package net.whimxiqal.journey.search;

import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Tunnel;

public abstract class DestinationGoalSearchSession extends GraphGoalSearchSession<DestinationSearchGraph> {

  protected final Cell destination;
  private final boolean persistentDestination;

  public DestinationGoalSearchSession(UUID callerId, Caller callerType,
                                      Cell origin, Cell destination,
                                      boolean persistentOrigin, boolean persistentDestination) {
    super(callerId, callerType, origin, persistentOrigin);
    this.destination = destination;
    this.persistentDestination = persistentDestination;
  }

  @Override
  DestinationSearchGraph createSearchGraph() {
    return new DestinationSearchGraph(this, origin, destination);
  }

  @Override
  protected void initSearchExtra() {
    if (origin.domain() == destination.domain()) {
      stateInfo.searchGraph.addPathTrialOriginToDestination(this.modes, persistentOrigin && persistentDestination);
    }

    for (Integer domain : stateInfo.allDomains) {
      for (Tunnel pathTrialOriginTunnel : stateInfo.tunnelsByDestinationDomain.get(domain)) {
        for (Tunnel pathTrialDestinationTunnel : stateInfo.tunnelsByOriginDomain.get(domain)) {
          if (domain.equals(destination.domain())) {
            stateInfo.searchGraph.addPathTrialTunnelToDestination(pathTrialOriginTunnel, this.modes, persistentDestination);
          }
        }
      }
    }
  }
}
