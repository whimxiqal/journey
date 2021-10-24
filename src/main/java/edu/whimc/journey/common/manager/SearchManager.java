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

package edu.whimc.journey.common.manager;

import edu.whimc.journey.common.journey.Journey;
import edu.whimc.journey.common.navigation.Cell;
import edu.whimc.journey.common.search.SearchSession;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SearchManager<T extends Cell<T, D>, D, S extends SearchSession<T, D>, J extends Journey<T, D>> {

  protected final Map<UUID, J> playerJourneys = new ConcurrentHashMap<>();
  protected final Map<UUID, T> playerLocations = new ConcurrentHashMap<>();
  protected final Map<UUID, S> playerSearches = new ConcurrentHashMap<>();

  public J putJourney(@NotNull UUID playerUuid, J journey) {
    J oldJourney = this.playerJourneys.put(playerUuid, journey);
    if (oldJourney != null) {
      oldJourney.stop();
    }
    return oldJourney;
  }

  @Nullable
  public J removeJourney(@NotNull UUID playerUuid) {
    J oldJourney = playerJourneys.remove(playerUuid);
    if (oldJourney != null) {
      oldJourney.stop();
    }
    return oldJourney;
  }

  public boolean hasJourney(@NotNull UUID playerUuid) {
    return playerJourneys.containsKey(playerUuid);
  }

  @Nullable
  public J getJourney(@NotNull UUID playerUuid) {
    return playerJourneys.get(playerUuid);
  }

  @Nullable
  public S putSearch(@NotNull UUID playerUuid, S search) {
    return playerSearches.put(playerUuid, search);
  }

  @Nullable
  public S removeSearch(@NotNull UUID playerUuid) {
    return playerSearches.remove(playerUuid);
  }

  public boolean isSearching(@NotNull UUID playerUuid) {
    return playerSearches.containsKey(playerUuid);
  }

  @Nullable
  public S getSearch(@NotNull UUID playerUuid) {
    return playerSearches.get(playerUuid);
  }

  @Nullable
  public T putLocation(@NotNull UUID playerUuid, T locationCell) {
    return playerLocations.put(playerUuid, locationCell);
  }

  @Nullable
  public T getLocation(@Nullable UUID playerUuid) {
    return playerLocations.get(playerUuid);
  }

  public void stopAllJourneys() {
    playerJourneys.values().forEach(Journey::stop);
  }

}
