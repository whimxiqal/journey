package edu.whimc.indicator.spigot.cache;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.spigot.journey.PlayerJourney;
import edu.whimc.indicator.spigot.path.LocationCell;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class JourneyManager implements Listener {

  private final Map<UUID, PlayerJourney> playerJourneys = new HashMap<>();
  private final Map<UUID, LocationCell> playerLocations = new HashMap<>();

  public Optional<PlayerJourney> putPlayerJourney(@NotNull UUID playerUuid, PlayerJourney journey) {
    return Optional.ofNullable(this.playerJourneys.put(playerUuid, journey));
  }

  public Optional<PlayerJourney> getPlayerJourney(@NotNull UUID playerUuid) {
    return Optional.ofNullable(playerJourneys.get(playerUuid));
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    LocationCell cell = new LocationCell(event.getPlayer().getLocation());
    UUID playerUuid = event.getPlayer().getUniqueId();
    if (!playerJourneys.containsKey(playerUuid)) {
      return;
    }

    if (!playerLocations.containsKey(playerUuid)) {
      playerLocations.put(playerUuid, cell);
      return;
    }

    if (playerLocations.get(playerUuid).equals(cell)) {
      return;
    }

    playerLocations.put(playerUuid, cell);
    playerJourneys.get(playerUuid).visit(cell);
  }

  public void registerListeners(Indicator indicator) {
    Bukkit.getPluginManager().registerEvents(this, indicator);
  }
}
