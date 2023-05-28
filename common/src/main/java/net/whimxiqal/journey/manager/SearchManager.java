/*
 * MIT License
 *
 * Copyright (c) whimxiqal
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

package net.whimxiqal.journey.manager;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.message.Formatter;
import net.whimxiqal.journey.navigation.journey.JourneySession;
import net.whimxiqal.journey.navigation.journey.PlayerJourneySession;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.search.flag.Flags;
import net.whimxiqal.journey.util.Initializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A manager to handle search sessions and their corresponding journeys.
 */
public final class SearchManager implements Initializable {

  // A random UUID that represents the console, for identification purposes
  public static final UUID CONSOLE_UUID = UUID.randomUUID();
  // Currently-executing searches for each player
  private final Map<UUID, SearchSession> playerSearches = new ConcurrentHashMap<>();
  // Queued searches for each player to run after the currently-executing search stops
  private final ConcurrentHashMap<UUID, SearchSession> nextPlayerSearches = new ConcurrentHashMap<>();

  // Current journeying-sessions for players that have a completed search
  private final Map<UUID, PlayerJourneySession> playerJourneys = new ConcurrentHashMap<>();
  // Known player locations, updated lazily and used for updating the journey sessions
  private final Map<UUID, Cell> cachedPlayerLocations = new ConcurrentHashMap<>();

  // Task id for the task that updates players' locations
  private UUID locationUpdateTaskId;

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
   * Get the journey.
   *
   * @param callerId the caller id
   * @return the journey, or null if it doesn't exist
   */
  @Nullable
  public PlayerJourneySession getJourney(@NotNull UUID callerId) {
    return playerJourneys.get(callerId);
  }

  public void launchSearch(SearchSession session) {
    UUID caller = session.getCallerId();
    if (caller == null) {
      return;
    }

    Audience audience;
    switch (session.getCallerType()) {
      case PLAYER:
        audience = Journey.get().proxy().audienceProvider().player(caller);
        break;
      case CONSOLE:
        audience = Journey.get().proxy().audienceProvider().console();
        break;
      case OTHER:
      default:
        audience = Audience.empty();
        break;
    }

    // update player maps
    if (playerSearches.containsKey(caller)) {
      // save session for after the current search stops
      nextPlayerSearches.put(caller, session);
      playerSearches.get(caller).stop(true);
      return;
    }

    // launch
    doLaunchSearch(caller, audience, session);
  }

  /**
   * A helper method that may be called again for the purposes of re-queueing the request
   * if another search is still executing, and we must wait for it to stop
   *
   * @param caller   the caller
   * @param audience the audience of the caller
   * @param session  the session we wish to run
   */
  private void doLaunchSearch(UUID caller, Audience audience, SearchSession session) {
    Journey.get().statsManager().incrementSearches();
    playerSearches.put(caller, session);
    session.initialize();

    AtomicReference<TextComponent> hoverText = new AtomicReference<>(Component.text("Search Parameters").color(Formatter.THEME));
    Flags.allFlags.forEach(flag -> hoverText.set(hoverText.get()
        .append(Component.newline())
        .append(Component.text(flag.name() + ":").color(Formatter.DARK).decorate(TextDecoration.BOLD))
        .append(Component.text(session.flags().printValueFor(flag)).color(Formatter.GOLD))));
    audience.sendMessage(Component.text()
        .append(Formatter.prefix())
        .append(Formatter.hover(Component.text("Searching...").color(Formatter.INFO), hoverText.get())));

    int timeout = session.flags().getValueFor(Flags.TIMEOUT);
    session.search(timeout).thenRun(() -> Journey.get().proxy().schedulingManager().schedule(() -> {
      if (nextPlayerSearches.containsKey(caller)) {
        SearchSession newSession = nextPlayerSearches.remove(caller);
        doLaunchSearch(caller, audience, newSession);
      } else {
        playerSearches.remove(caller);
      }
    }, false));
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
  public SearchSession getSearch(@NotNull UUID callerId) {
    SearchSession session = playerSearches.get(callerId);
    if (session == null) {
      throw new NoSuchElementException("There is no search with callerId: " + callerId);
    }
    return session;
  }

  public void registerLocation(UUID playerUuid, Cell location) {
    PlayerJourneySession playerJourney = getJourney(playerUuid);

    if (playerJourney == null) {
      // We don't care about a player moving unless there's a journey happening
      return;
    }

    Cell currentCachedLocation = cachedPlayerLocations.get(playerUuid);
    if (currentCachedLocation != null && currentCachedLocation.equals(location)) {
      return;
    }

    cachedPlayerLocations.put(playerUuid, location);
    playerJourney.visit(location);
  }

  public void initialize() {
    // task for updating player locations lazily
    locationUpdateTaskId = Journey.get().proxy().schedulingManager().scheduleRepeat(() -> {
      for (UUID journeyingPlayer : playerJourneys.keySet()) {
        Journey.get().proxy()
            .platform()
            .onlinePlayer(journeyingPlayer)
            .ifPresent(player -> registerLocation(journeyingPlayer, player.location()));
      }
    }, false, 5);
  }

  public void shutdown() {
    // cancel all searches
    playerSearches.values().forEach(session -> session.stop(false));
    // stop all journeys
    playerJourneys.values().forEach(JourneySession::stop);
    if (locationUpdateTaskId != null) {
      Journey.get().proxy().schedulingManager().cancelTask(locationUpdateTaskId);
      locationUpdateTaskId = null;
    }
  }

}
