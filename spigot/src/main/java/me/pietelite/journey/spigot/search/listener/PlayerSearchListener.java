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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pietelite.journey.spigot.search.listener;

import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.message.Formatter;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.Itinerary;
import me.pietelite.journey.common.navigation.JourneySession;
import me.pietelite.journey.common.search.ResultState;
import me.pietelite.journey.common.search.SearchSession;
import me.pietelite.journey.spigot.JourneySpigot;
import me.pietelite.journey.spigot.search.event.SpigotFoundSolutionEvent;
import me.pietelite.journey.spigot.search.event.SpigotIgnoreCacheSearchEvent;
import me.pietelite.journey.spigot.search.event.SpigotSearchEvent;
import me.pietelite.journey.spigot.search.event.SpigotStartItinerarySearchEvent;
import me.pietelite.journey.spigot.search.event.SpigotStartPathSearchEvent;
import me.pietelite.journey.spigot.search.event.SpigotStartSearchEvent;
import me.pietelite.journey.spigot.search.event.SpigotStopItinerarySearchEvent;
import me.pietelite.journey.spigot.search.event.SpigotStopPathSearchEvent;
import me.pietelite.journey.spigot.search.event.SpigotStopSearchEvent;
import me.pietelite.journey.common.navigation.PlayerJourneySession;
import me.pietelite.journey.spigot.search.PlayerDestinationGoalSearchSession;
import me.pietelite.journey.spigot.search.PlayerSessionState;
import me.pietelite.journey.spigot.search.SpigotPlayerSearchSession;
import me.pietelite.journey.spigot.util.Format;
import me.pietelite.journey.spigot.util.SpigotUtil;
import me.pietelite.journey.spigot.util.TimeUtil;
import java.util.Optional;
import java.util.UUID;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * A listener with a series of event handlers to perform general management operations
 * of running {@link PlayerDestinationGoalSearchSession}s.
 */
public class PlayerSearchListener implements Listener {

  private static final double STEVE_RUNNING_SPEED = 5.621;  // blocks per second

  /**
   * Handle the start of search sessions.
   * The instance must be saved and the last one running has to be stopped.
   *
   * @param event the event
   */
  @EventHandler
  public void startSearchEvent(SpigotStartSearchEvent event) {
    getPlayerSearch(event).ifPresent(search -> {
      Player player = Bukkit.getPlayer(event.getSearchEvent().getSession().getCallerId());
      if (player != null) {

        Journey.get().debugManager().broadcast(Formatter.debug("Started a search for player ___", player.getName()));
        Journey.get().debugManager().broadcast(Formatter.debug("Modes: ___", search.getSession().modes().size()));
        Journey.get().debugManager().broadcast(Formatter.debug("Ports: ___", search.getSession().ports().size()));

        SearchSession oldSession = Journey.get().searchManager().getSearch(player.getUniqueId());

        if (oldSession != null && oldSession.getState() == ResultState.RUNNING) {
          player.spigot().sendMessage(Format.info("Canceling other search..."));
        }

        Journey.get().searchManager().putSearch(player.getUniqueId(), search.getSession());
      }
    });
  }

  /**
   * Handle the event fired when a new solution is found.
   * Send the found {@link Itinerary} to the running {@link JourneySession}
   * as a prospective itinerary in case the player wants to use it.
   *
   * @param event the event
   */
  @EventHandler
  public void foundSolutionEvent(SpigotFoundSolutionEvent event) {
    getPlayerSearch(event).ifPresent(search -> {
      // Need to update the session state
      PlayerSessionState playerSessionState = search.getSessionState();

      Player player = Bukkit.getPlayer(search.getSession().getCallerId());
      if (player == null) {
        return;
      }

      Journey.get().debugManager().broadcast(Formatter.debug("Found a solution to a search for player ___", player.getName()));

      Itinerary itinerary = event.getSearchEvent().getItinerary();
      if (playerSessionState.wasSolutionPresented()) {
        PlayerJourneySession journey = Journey.get().searchManager().getJourney(player.getUniqueId());
        if (journey != null) {
          journey.setProspectiveItinerary(itinerary);
          player.spigot().sendMessage(Format.info("A faster path to your destination "
              + "was found from your original location."));
          player.spigot().sendMessage(Format.chain(Format.info("Run "),
              Format.command("/journey accept", "Accept an incoming trail request"),
              Format.textOf(Format.INFO + " to accept.")));
          return;
        }
      }

      if (playerSessionState.wasSolved()) {
        Bukkit.getScheduler().cancelTask(playerSessionState.getSuccessNotificationTaskId());
      }
      playerSessionState.setSolved(true);

      // Create a journey that is completed when the player reaches within 3 blocks of the endpoint
      PlayerJourneySession journey = new PlayerJourneySession(player.getUniqueId(), search.getSession(), itinerary);
      journey.run();

      // Save the journey
      Journey.get().searchManager().putJourney(player.getUniqueId(), journey);

      // Set up a success notification that will be cancelled if a better one is found in some amount of time
      playerSessionState.setSuccessNotificationTaskId(Bukkit.getScheduler()
          .runTaskLater(JourneySpigot.getInstance(),
              () -> {
                player.spigot().sendMessage(new ComponentBuilder()
                    .append(Format.PREFIX)
                    .append(new ComponentBuilder()
                        .append("Success! Please follow the path.")
                        .color(Format.SUCCESS.asBungee())
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new Text(new ComponentBuilder("Search Statistics")
                                .color(Format.THEME.asBungee())
                                .bold(true)
                                .create()),
                            new Text(new ComponentBuilder("\nWalk Time: ")
                                .bold(false)
                                .color(ChatColor.DARK_GRAY.asBungee())
                                .append(TimeUtil.toSimpleTime(
                                    Math.round(event.getSearchEvent().getItinerary().getLength()
                                        / STEVE_RUNNING_SPEED)))
                                .color(Format.ACCENT2.asBungee())
                                .create()),
                            new Text(new ComponentBuilder("\nDistance: ")
                                .bold(false)
                                .color(ChatColor.DARK_GRAY.asBungee())
                                .append(Math.round(event.getSearchEvent().getItinerary().getLength())
                                    + " blocks")
                                .color(Format.ACCENT2.asBungee())
                                .create()),
                            new Text(new ComponentBuilder("\nSearch Time: ")
                                .bold(false)
                                .color(ChatColor.DARK_GRAY.asBungee())
                                .append(TimeUtil.toSimpleTime(
                                    Math.round((double) event.getSearchEvent().getExecutionTime() / 1000)))
                                .color(Format.ACCENT2.asBungee())
                                .create())))
                        .create())
                    .create());
                playerSessionState.setSolutionPresented(true);
              },
              20 /* one second (20 ticks) */)
          .getTaskId());
    });
  }

  /**
   * Handle the stop search event.
   * We need to remove the instance from memory storage and stop any running tasks.
   *
   * @param event the event
   */
  @EventHandler
  public void stopSearchEvent(SpigotStopSearchEvent event) {
    getPlayerSearch(event).ifPresent(search -> {
      // Send failure message if we finished unsuccessfully
      Player player = Bukkit.getPlayer(search.getSession().getCallerId());
      if (player != null) {

        Journey.get().debugManager().broadcast(Formatter.debug("Stopping a search for player ___" + player.getName()));
        Journey.get().debugManager().broadcast(Formatter.debug("Status: ___" + search.getSession().getState()));

        switch (search.getSession().getState()) {
          case STOPPED_FAILED:
            player.spigot().sendMessage(
                Format.error("Search ended. Either there's no path to it, or it's too far away!"));
            break;
          case STOPPED_CANCELED:
            player.spigot().sendMessage(
                Format.info("Search canceled."));
            break;
          case STOPPED_SUCCESSFUL:
            /* Don't say anything. They were already notified of the successful solutions. */
            break;
          default:
            Bukkit.getLogger().warning("A player search session stopped while in the "
                + search.getSession().getState() + " state");
        }
        // Remove from the searching set, if the saved session is the one that is currently stopping here.
        //  This check is necessary because another session could have been started while this one was running

        SearchSession savedSession = Journey.get().searchManager().getSearch(player.getUniqueId());
        if (savedSession != null && savedSession.getUuid().equals(search.getSession().getUuid())) {
          Journey.get().searchManager().removeSearch(player.getUniqueId());
        }
      }
    });
  }

  /**
   * Handle the event when an itinerary search begins for those that were caused by a player.
   *
   * @param event the event
   */
  @EventHandler
  public void startItinerarySearchEvent(SpigotStartItinerarySearchEvent event) {
    getPlayerSearch(event).ifPresent(search -> {
      Player player = Bukkit.getPlayer(search.getSession().getCallerId());
      if (player != null) {
        Journey.get().debugManager().broadcast(Formatter.debug("Started an itinerary search for player ___", player.getName()));
      }
    });
  }

  /**
   * Handle the event when an itinerary search ends for those that were caused by a player.
   *
   * @param event the event
   */
  @EventHandler
  public void stopItinerarySearchEvent(SpigotStopItinerarySearchEvent event) {
    getPlayerSearch(event).ifPresent(search -> {
      Player player = Bukkit.getPlayer(search.getSession().getCallerId());
      if (player != null) {
        Journey.get().debugManager().broadcast(Formatter.debug("Stopped an itinerary search for player ___", player.getName()));
        Journey.get().debugManager().broadcast(Formatter.debug("Status: ___", event.getSearchEvent().getItineraryTrial().getState()));
      }
    });
  }

  /**
   * Handle the event when a path search begins for those that were caused by a player.
   *
   * @param event the event
   */
  @EventHandler
  public void startPathSearchEvent(SpigotStartPathSearchEvent event) {
    getPlayerSearch(event).ifPresent(search -> {
      Player player = Bukkit.getPlayer(search.getSession().getCallerId());
      if (player != null) {
        Journey.get().debugManager().broadcast(Formatter.debug("Started a path search for player ___", player.getName()));
      }
    });
  }

  /**
   * Handle the event when a path search ends for those that were caused by a player.
   *
   * @param event the event
   */
  @EventHandler
  public void stopPathSearchEvent(SpigotStopPathSearchEvent event) {
    getPlayerSearch(event).ifPresent(search -> {
      Player player = Bukkit.getPlayer(search.getSession().getCallerId());
      if (player != null) {
        Journey.get().debugManager().broadcast(Formatter.debug("Stopped a path search for player ___", player.getName()));
        Journey.get().debugManager().broadcast(Formatter.debug("Status: ___", event.getSearchEvent().getPathTrial().getState()));
      }
    });
  }

  /**
   * Handle the event when a search stops considering cached paths for those that were caused by a player.
   *
   * @param event the event
   */
  @EventHandler
  public void ignoreCacheSearchEvent(SpigotIgnoreCacheSearchEvent event) {
    getPlayerSearch(event).ifPresent(search -> {
      Player player = Bukkit.getPlayer(search.getSession().getCallerId());
      if (player != null) {
        if (!search.getSession().getState().isSuccessful()) {
          player.spigot().sendMessage(Format.warn("There probably isn't a solution, "
              + "but the search will continue, just in case!"));
        }
        Journey.get().debugManager().broadcast(Formatter.debug("Ignoring cache in search for ___", player.getName()));
        Journey.get().debugManager().broadcast(Formatter.debug("Status: ___", event.getSearchEvent().getSession().getState()));
      }
    });
  }


  /**
   * Handler for when players move throughout the world.
   * This allows us to update the last known location so player journeys
   * know which particles to show.
   *
   * @param event the event
   */
  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {

    if (event.getTo() == null) {
      return;
    }
    Cell cell = SpigotUtil.cell(event.getTo());
    UUID playerUuid = event.getPlayer().getUniqueId();
    PlayerJourneySession playerJourney = Journey.get().searchManager().getJourney(playerUuid);

    if (playerJourney == null) {
      // We don't care about a player moving unless there's a journey happening
      return;
    }

    Cell currentLocation = Journey.get().searchManager().getLocation(playerUuid);
    if (currentLocation == null) {
      Journey.get().searchManager().putLocation(playerUuid, cell);
      return;
    }

    if (currentLocation.equals(cell)) {
      return;
    }

    Journey.get().searchManager().putLocation(playerUuid, cell);
    playerJourney.visit(cell);
  }

  /**
   * Handle the player quit event.
   *
   * @param event the event
   */
  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    // Perform quit logic. Currently, nothing.
  }

  @SuppressWarnings("unchecked")
  private Optional<SpigotPlayerSearchSession<SearchSession>> getPlayerSearch(
      SpigotSearchEvent<?> event) {
    if (event.getSearchEvent().getSession() instanceof SpigotPlayerSearchSession) {
      return Optional.of((SpigotPlayerSearchSession<SearchSession>)
          event.getSearchEvent().getSession());
    } else {
      return Optional.empty();
    }
  }

}
