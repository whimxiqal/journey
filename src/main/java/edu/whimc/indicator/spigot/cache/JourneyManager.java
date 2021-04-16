/*
 * Copyright 2021 Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package edu.whimc.indicator.spigot.cache;

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.spigot.journey.PlayerJourney;
import edu.whimc.indicator.spigot.path.LocationCell;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JourneyManager implements Listener {

  private final Map<UUID, PlayerJourney> playerJourneys = new ConcurrentHashMap<>();
  private final Map<UUID, LocationCell> playerLocations = new ConcurrentHashMap<>();
  private final Set<UUID> searchingPlayers = ConcurrentHashMap.newKeySet();

  public Optional<PlayerJourney> putPlayerJourney(@NotNull UUID playerUuid, PlayerJourney journey) {
    PlayerJourney oldJourney = this.playerJourneys.put(playerUuid, journey);
    if (oldJourney != null) {
      oldJourney.stop();
    }
    return Optional.ofNullable(oldJourney);
  }

  public Optional<PlayerJourney> getPlayerJourney(@NotNull UUID playerUuid) {
    return Optional.ofNullable(playerJourneys.get(playerUuid));
  }

  public Optional<PlayerJourney> removePlayerJourney(@NotNull UUID playerUuid) {
    PlayerJourney oldJourney = playerJourneys.remove(playerUuid);
    if (oldJourney != null) {
      oldJourney.stop();
    }
    return Optional.ofNullable(oldJourney);
  }

  public boolean startSearching(@NotNull UUID playerUuid) {
    return searchingPlayers.add(playerUuid);
  }

  public boolean stopSearching(@NotNull UUID playerUuid) {
    return searchingPlayers.remove(playerUuid);
  }

  public boolean isSearching(@NotNull UUID playerUuid) {
    return searchingPlayers.contains(playerUuid);
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    if (event.getTo() == null) return;
    LocationCell cell = new LocationCell(event.getTo());
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
