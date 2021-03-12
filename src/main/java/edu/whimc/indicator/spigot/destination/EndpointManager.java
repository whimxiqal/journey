package edu.whimc.indicator.spigot.destination;

import edu.whimc.indicator.api.path.Endpoint;
import edu.whimc.indicator.spigot.path.LocationCell;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EndpointManager {

  private final Map<UUID, Endpoint<JavaPlugin, LocationCell, World>> endpoints = new HashMap<>();

  public Endpoint<JavaPlugin, LocationCell, World> get(UUID playerUuid) {
    return endpoints.get(playerUuid);
  }

  public void put(UUID playerUuid, Endpoint<JavaPlugin, LocationCell, World> destination) {
    this.endpoints.put(playerUuid, destination);
  }

  public boolean containsKey(UUID playerUuid) {
    return this.endpoints.containsKey(playerUuid);
  }

}
