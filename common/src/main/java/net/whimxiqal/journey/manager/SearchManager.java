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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.message.Formatter;
import net.whimxiqal.journey.message.Messages;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.navigation.Navigator;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.search.flag.FlagSet;
import net.whimxiqal.journey.search.flag.Flags;
import net.whimxiqal.journey.util.TimeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A manager to handle search sessions and their corresponding journeys.
 * <p>
 * This is single-threaded access only, so only make calls on main server thread
 */
public final class SearchManager {

  // A random UUID that represents the console, for identification purposes
  public static final UUID CONSOLE_UUID = UUID.randomUUID();
  // Currently-executing searches for each player
  private final Map<UUID, SearchSession> playerSearches = new HashMap<>();
  // Queued searches for each player to run after the currently-executing search stops
  private final HashMap<UUID, QueuedSearch> nextPlayerSearches = new HashMap<>();

  private final Map<UUID, FlagSet> flagPreferences = new HashMap<>();

  /**
   * Start searching with the given search session, and register it with this manager to enforce
   * that no more than one session is executing per player and also to store the {@link Navigator}
   * if it completes successfully.
   *
   * @param session the session
   * @return the future for the result
   */
  public Future<SearchSession.Result> launchIngameSearch(SearchSession session) {
    CompletableFuture<SearchSession.Result> future = new CompletableFuture<>();
    if (!Journey.get().proxy().schedulingManager().isMainThread()) {
      throw new RuntimeException();  // programmer error: this must be called on main thread
    }
    UUID caller = session.getCallerId();
    if (caller == null) {
      future.complete(null);  // never ran search
      return future;
    }

    // update player maps
    SearchSession existingSession = playerSearches.get(caller);
    if (existingSession != null) {
      // we need to cancel current session, and save session for after the current search stops
      QueuedSearch existingQueuedSearch = nextPlayerSearches.get(caller);
      if (existingQueuedSearch != null) {
        // there was already a next session scheduled, lets get rid of it
        existingQueuedSearch.future.complete(null);  // null because the search never actually ran
      }
      nextPlayerSearches.put(caller, new QueuedSearch(session, future));  // schedule
      Journey.logger().debug(existingSession + ": another search was requested, so canceling this one");
      existingSession.stop(true);  // cancel existing
      return future;
    }

    // launch
    doLaunchSearch(session, future);
    return future;
  }

  /**
   * A helper method that may be called again for the purposes of re-queueing the request
   * if another search is still executing, and we must wait for it to stop
   *
   * @param session the session we wish to run
   */
  private void doLaunchSearch(SearchSession session, CompletableFuture<SearchSession.Result> future) {
    Journey.get().statsManager().incrementSearches();
    UUID caller = Objects.requireNonNull(session.getCallerId());  // launches here in the search manager must have caller ids
    playerSearches.put(caller, session);

    Audience audience = switch (session.getCallerType()) {
      case PLAYER -> Journey.get().proxy().audienceProvider().player(caller);
      case CONSOLE -> Journey.get().proxy().audienceProvider().console();
      default -> Audience.empty();
    };

    try {
      session.initialize();
    } catch (Exception e) {
      // the initialize function can cause unknown errors because it uses registered functions from the API,
      //  so we want to handle other dev's bugs gracefully
      Messages.COMMAND_INTERNAL_ERROR.sendTo(audience, Formatter.ERROR);
      e.printStackTrace();
      playerSearches.remove(caller);
      return;
    }

    AtomicReference<TextComponent> hoverText = new AtomicReference<>(Component.text("Search Parameters").color(Formatter.THEME));
    Flags.ALL_FLAGS.forEach(flag -> hoverText.set(hoverText.get()
        .append(Component.newline())
        .append(Component.text(flag.name() + ": ").color(Formatter.DARK))
        .append(Component.text(session.flags().printValueFor(flag)).color(Formatter.GOLD))));

    audience.sendMessage(Formatter.hover(Messages.COMMAND_SEARCH_SEARCHING.resolve(Formatter.INFO), hoverText.get()));

    session.search().thenAccept(result -> {
      if (result == null) {
        Journey.logger().debug(session + ": session never ran and was unscheduled");
        future.complete(null);
        return;
      }

      Journey.logger().debug(session + ": search complete");
      // schedule the completion logic back on the main thread
      Journey.get().proxy().schedulingManager().schedule(() -> {
        switch (result.state()) {
          case STOPPED_SUCCESSFUL -> {
            Itinerary itinerary = result.itinerary();
            if (itinerary != null) {
              audience.sendMessage(Formatter.prefix()
                  .append(Component.text(Messages.COMMAND_SEARCH_SUCCESS.resolve() + " ").color(Formatter.SUCCESS))
                  .append(Component.text("[").color(Formatter.DARK)
                      .append(Component.text("stats").color(Formatter.DULL).decorate(TextDecoration.ITALIC))
                      .append(Component.text("]").color(Formatter.DARK))
                      .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT,
                          Component.text()
                              .append(Component.text("Search Statistics").color(Formatter.THEME).decorate(TextDecoration.BOLD))
                              .append(Component.newline())
                              .append(Component.text("Walk Time: ")
                                  .color(Formatter.DULL)
                                  .append(Component.text(TimeUtil.toSimpleTime(Math.round(itinerary.cost() / 5.621 /* Steve's running speed. */)))
                                      .color(Formatter.ACCENT)))
                              .append(Component.newline())
                              .append(Component.text("Distance: ")
                                  .color(Formatter.DULL)
                                  .append(Component.text(Math.round(itinerary.cost()) + " blocks")
                                      .color(Formatter.ACCENT)))
                              .append(Component.newline())
                              .append(Component.text("Search Time: ")
                                  .color(Formatter.DULL)
                                  .append(Component.text(TimeUtil.toSimpleTime(
                                          Math.round((double) session.executionTime() / 1000)))
                                      .color(Formatter.ACCENT)))
                              .build()))));

              Journey.get().navigatorManager().stopNavigators(session.agent().uuid());
              Journey.get().navigatorManager().startNavigating(session.agent(), itinerary.steps(), session.flags().getValueFor(Flags.NAVIGATOR));
            } else {
              // itinerary is null, so we have no Navigator to start
              Messages.COMMAND_SEARCH_SUCCESS.sendTo(audience, Formatter.SUCCESS);
            }
          }
          case STOPPED_CANCELED -> Messages.COMMAND_SEARCH_CANCELED.sendTo(audience, Formatter.ERROR);
          case STOPPED_FAILED -> Messages.COMMAND_SEARCH_FAILED.sendTo(audience, Formatter.WARN);
          case STOPPED_ERROR -> Messages.COMMAND_SEARCH_ERROR.sendTo(audience, Formatter.ERROR);
          default -> throw new RuntimeException();  // programmer error, should never finish the search with this state
        }

        // run next search, if there is another one queued
        playerSearches.remove(caller);
        if (nextPlayerSearches.containsKey(caller)) {
          QueuedSearch newSession = nextPlayerSearches.remove(caller);
          doLaunchSearch(newSession.session, newSession.future);
        }
        future.complete(result);
      }, false);
    });
  }

  /**
   * Get the stored search.
   *
   * @param callerId the caller id
   * @return the stored search, or null if there is none
   */
  public @Nullable SearchSession getSearch(@NotNull UUID callerId) {
    return playerSearches.get(callerId);
  }

  /**
   * Get the caller's flag preferences. If "persistent" is set, then the returned flag set will be stored
   * in memory for future retrieval. If the flag set is just used for reading, don't set "persistent" so that
   * it needn't be saved in memory. Must be called on main thread.
   *
   * @param callerId   the caller
   * @param persistent whether to keep the returned flag set in memory or not, so that edits affect the stored set
   * @return the flag set of preferences
   */
  public @NotNull FlagSet getFlagPreferences(UUID callerId, boolean persistent) {
    FlagSet flagSet = flagPreferences.get(callerId);
    if (flagSet == null) {
      flagSet = new FlagSet();
      if (persistent) {
        flagPreferences.put(callerId, flagSet);
      }
      return flagSet;
    }
    if (flagSet.isEmpty() && !persistent) {
      flagPreferences.remove(callerId);  // don't need this, clean up memory
    }
    return flagSet;
  }

  public void shutdown() {
    Journey.logger().debug("[Search Manager] Shutting down...");
    // cancel all searches
    playerSearches.values().forEach(session -> session.stop(false));

    // now wait for all sessions to stop
    for (SearchSession canceledSession : playerSearches.values()) {
      try {
        canceledSession.future().get();
      } catch (InterruptedException | ExecutionException e) {
        // just ignore and continue
      }
    }
  }

  private record QueuedSearch(SearchSession session, CompletableFuture<SearchSession.Result> future) {
  }

}
