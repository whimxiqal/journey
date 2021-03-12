package edu.whimc.indicator.spigot.destination;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EndpointManager {

  private final Map<UUID, EndpointImpl> endpoints = new HashMap<>();

  public EndpointImpl get(UUID playerUuid) {
    return endpoints.get(playerUuid);
  }

  public void put(UUID playerUuid, EndpointImpl destination) {
    this.endpoints.put(playerUuid, destination);
  }

  public boolean containsKey(UUID playerUuid) {
    return this.endpoints.containsKey(playerUuid);
  }

}
