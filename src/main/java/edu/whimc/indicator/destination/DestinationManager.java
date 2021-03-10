package edu.whimc.indicator.destination;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DestinationManager {

  private final Map<UUID, IndicatorDestination> destinations = new HashMap<>();

  public IndicatorDestination get(UUID playerUuid) {
    return destinations.get(playerUuid);
  }

  public void put(UUID playerUuid, IndicatorDestination destination) {
    this.destinations.put(playerUuid, destination);
  }

  public boolean containsKey(UUID playerUuid) {
    return this.destinations.containsKey(playerUuid);
  }

}
