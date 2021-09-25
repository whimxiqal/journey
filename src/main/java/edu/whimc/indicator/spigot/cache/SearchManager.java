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
import edu.whimc.indicator.common.search.Search;
import edu.whimc.indicator.spigot.journey.PlayerJourney;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class SearchManager implements Listener {

  private final Map<UUID, PlayerJourney> playerJourneys = new ConcurrentHashMap<>();
  private final Map<UUID, LocationCell> playerLocations = new ConcurrentHashMap<>();
  private final Map<UUID, Search<LocationCell, World>> playerSearches = new ConcurrentHashMap<>();

  public PlayerJourney putPlayerJourney(@NotNull UUID playerUuid, PlayerJourney journey) {
    PlayerJourney oldJourney = this.playerJourneys.put(playerUuid, journey);
    if (oldJourney != null) {
      oldJourney.stop();
    }
    return oldJourney;
  }


  public PlayerJourney removePlayerJourney(@NotNull UUID playerUuid) {
    PlayerJourney oldJourney = playerJourneys.remove(playerUuid);
    if (oldJourney != null) {
      oldJourney.stop();
    }
    return oldJourney;
  }

  public boolean hasPlayerJourney(@NotNull UUID playerUuid) {
    return playerJourneys.containsKey(playerUuid);
  }

  public PlayerJourney getPlayerJourney(@NotNull UUID playerUuid) {
    return playerJourneys.get(playerUuid);
  }


  public Search<LocationCell, World> putSearch(@NotNull UUID playerUuid, Search<LocationCell, World> search) {
    return playerSearches.put(playerUuid, search);
  }

  public Search<LocationCell, World> removeSearch(@NotNull UUID playerUuid) {
    return playerSearches.remove(playerUuid);
  }

  public boolean isSearching(@NotNull UUID playerUuid) {
    return playerSearches.containsKey(playerUuid);
  }

  public Search<LocationCell, World> getSearch(@NotNull UUID playerUuid) {
    return playerSearches.get(playerUuid);
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

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    // Stop the search so we don't waste memory on someone who isn't here anymore
    Search<LocationCell, World> currentSearch = getSearch(event.getPlayer().getUniqueId());
    if (currentSearch != null) {
      currentSearch.cancel();
    }
  }

  public void registerListeners(Indicator indicator) {
    Bukkit.getPluginManager().registerEvents(this, indicator);
  }
}
