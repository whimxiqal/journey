/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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
 * THE SOFTWARE ISearchSession PROVIDED "ASearchSession IS", WITHOUT WARRANTY OF ANY KIND, EXPRESSearchSession OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIESearchSession OF MERCHANTABILITY, FITNESSearchSession FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORSearchSession OR
 * COPYRIGHT HOLDERSearchSession BE LIABLE FOR ANY CLAIM, DAMAGESearchSession OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGSearchSession IN THE SOFTWARE.
 */

package me.pietelite.journey.common.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.config.Settings;
import me.pietelite.journey.common.message.Formatter;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.journey.JourneySession;
import me.pietelite.journey.common.navigation.journey.PlayerJourneySession;
import me.pietelite.journey.common.search.SearchSession;
import me.pietelite.journey.common.search.flag.Flags;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A manager to handle search sessions and their corresponding journeys.
 */
public class SearchManager {

  protected final Map<UUID, PlayerJourneySession> playerJourneys = new ConcurrentHashMap<>();
  protected final Map<UUID, Cell> playerLocations = new ConcurrentHashMap<>();
  protected final Map<UUID, SearchSession> playerSearches = new ConcurrentHashMap<>();

  /**
   * Store a journey. Stops the previously running journey if there was one.
   *
   * @param callerId the caller id
   * @param journey  the journey
   * @return the old journey
   */
  public PlayerJourneySession putJourney(@NotNull UUID callerId, PlayerJourneySession journey) {
    PlayerJourneySession oldJourney = this.playerJourneys.put(callerId, journey);
    if (oldJourney != null) {
      oldJourney.stop();
    }
    return oldJourney;
  }

  /**
   * Remove a journey from storage and stop the journey.
   *
   * @param callerId the caller id
   * @return the removed journey
   */
  @Nullable
  public PlayerJourneySession removeJourney(@NotNull UUID callerId) {
    PlayerJourneySession oldJourney = playerJourneys.remove(callerId);
    if (oldJourney != null) {
      oldJourney.stop();
    }
    return oldJourney;
  }

  /**
   * Whether there is a journey stored.
   *
   * @param callerId the caller id
   * @return true if there is a journey
   */
  public boolean hasJourney(@NotNull UUID callerId) {
    return playerJourneys.containsKey(callerId);
  }

  /**
   * Get the journey.
   *
   * @param callerId the caller id
   * @return the journey, or null if it doesn't exist
   */
  @Nullable
  public PlayerJourneySession getJourney(@NotNull UUID callerId) {
    return playerJourneys.get(callerId);
  }

  public boolean launchSearch(SearchSession session) {
    UUID player = session.getCallerId();
    if (player == null) {
      return false;
    }

    if (playerSearches.containsKey(player)) {
      return false;
    }

    Audience audience;
    switch (session.getCallerType()) {
      case PLAYER:
        audience = Journey.get().proxy().audienceProvider().player(player);
        break;
      case OTHER:
      default:
        audience = Audience.empty();
        break;
    }

    // Set up a "Working..." message if it takes too long
    AtomicReference<TextComponent> hoverText = new AtomicReference<>(Component.text("Search Parameters").color(Formatter.THEME));
    session.flags().forEach((flag, val) -> {
      hoverText.set(hoverText.get().append(Component.newline())
          .append(Component.text(flag.name()).color(Formatter.DARK).decorate(TextDecoration.BOLD)));
      val.ifPresent(s -> hoverText.set(hoverText.get().append(Component.text(":").color(Formatter.DARK).decorate(TextDecoration.BOLD))
          .append(Component.text(s))));
    });
    audience.sendMessage(Component.text()
        .append(Formatter.prefix())
        .append(Formatter.hover(Component.text("Searching...").color(Formatter.INFO), hoverText.get())));

    // SEARCH
    // Search... this may take a long time
    int timeout = session.flags().valueOrGetDefault(Flags.TIMEOUT, Settings.DEFAULT_SEARCH_TIMEOUT::getValue);
    playerSearches.put(player, session);
    session.search(timeout).thenRun(() -> Journey.get().proxy().schedulingManager().schedule(() -> playerSearches.remove(player), false, 0));
    return true;
  }

  /**
   * Remove the stored search and cancel it.
   *
   * @param callerId the caller id
   * @return the previous search, or null if there was none
   */
  @Nullable
  public SearchSession removeSearch(@NotNull UUID callerId) {
    SearchSession oldSearch = playerSearches.remove(callerId);
    if (oldSearch != null) {
      oldSearch.stop();
    }
    return oldSearch;
  }

  /**
   * Get whether there is a search stored for a caller.
   *
   * @param callerId the caller id
   * @return whether there is a search stored
   */
  public boolean isSearching(@NotNull UUID callerId) {
    return playerSearches.containsKey(callerId);
  }

  /**
   * Get the stored search.
   *
   * @param callerId the caller id
   * @return the stored search, or null if there is none
   */
  @Nullable
  public SearchSession getSearch(@NotNull UUID callerId) {
    return playerSearches.get(callerId);
  }

  /**
   * Update the last known location of a certain caller.
   *
   * @param callerId the caller id
   * @param location the location
   * @return the previous location, or null if there was none
   */
  @Nullable
  public Cell putLocation(@NotNull UUID callerId, Cell location) {
    return playerLocations.put(callerId, location);
  }

  /**
   * Get the last known location of a certain caller.
   * This is the last one updated with {@link #putLocation}.
   *
   * @param callerId the caller id
   * @return the location
   */
  @Nullable
  public Cell getLocation(@Nullable UUID callerId) {
    return playerLocations.get(callerId);
  }

  /**
   * Cancel all the saved running searches.
   */
  public void cancelAllSearches() {
    playerSearches.values().forEach(SearchSession::stop);
  }

  /**
   * Stop all the saved running journeys.
   */
  public void stopAllJourneys() {
    playerJourneys.values().forEach(JourneySession::stop);
  }

}
