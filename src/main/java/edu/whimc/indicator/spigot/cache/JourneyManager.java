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
