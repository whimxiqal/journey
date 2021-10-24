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

package edu.whimc.journey.spigot.search.listener;

import edu.whimc.journey.common.navigation.Itinerary;
import edu.whimc.journey.common.search.ResultState;
import edu.whimc.journey.common.search.SearchSession;
import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.journey.PlayerJourney;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.search.PlayerSearchSession;
import edu.whimc.journey.spigot.search.SessionState;
import edu.whimc.journey.spigot.search.event.SpigotFoundSolutionEvent;
import edu.whimc.journey.spigot.search.event.SpigotSearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotStartSearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotStopSearchEvent;
import edu.whimc.journey.spigot.util.Format;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SearchListener implements Listener {

  @EventHandler
  public void startSearchEvent(SpigotStartSearchEvent event) {
    PlayerSearchSession session = getPlayerSession(event);
    if (session != null) {
      Player player = Bukkit.getPlayer(event.getSearchEvent().getSession().getCallerId());
      if (player != null) {
        PlayerSearchSession oldSession = JourneySpigot.getInstance()
            .getSearchManager()
            .putSearch(player.getUniqueId(), session);
        if (oldSession != null) {
          if (oldSession.getState() != ResultState.SUCCESSFUL) {
            player.spigot().sendMessage(Format.info("Canceling previously running search..."));
          }
          oldSession.cancel();
        }
      }
    }
  }

  @EventHandler
  public void foundSolutionEvent(SpigotFoundSolutionEvent event) {
    PlayerSearchSession session = getPlayerSession(event);
    if (session == null) {
      return;
    }
    SessionState sessionState = session.getSessionInfo();

    Player player = Bukkit.getPlayer(session.getCallerId());
    if (player == null) {
      return;
    }

    Itinerary<LocationCell, World> itinerary = event.getSearchEvent().getItinerary();
    if (sessionState.wasSolutionPresented()) {
      PlayerJourney journey = JourneySpigot.getInstance().getSearchManager().getJourney(player.getUniqueId());
      if (journey != null) {
        journey.setProspectiveItinerary(itinerary);
        player.spigot().sendMessage(Format.info("A faster path to your destination "
            + "was found from your original location."));
        player.spigot().sendMessage(Format.chain(Format.info("Run "),
            Format.command("/nav accept", "Accept an incoming trail request"),
            Format.textOf(Format.INFO + " to accept.")));
        return;
      }
    }

    if (sessionState.wasSolved()) {
      Bukkit.getScheduler().cancelTask(sessionState.getSuccessNotificationTaskId());
    }
    sessionState.setSolved(true);

    // Create a journey that is completed when the player reaches within 3 blocks of the endpoint
    PlayerJourney journey = new PlayerJourney(player.getUniqueId(), session, itinerary);
    journey.illuminateTrail();

    // Save the journey
    JourneySpigot.getInstance().getSearchManager().putJourney(player.getUniqueId(), journey);

    // Set up a success notification that will be cancelled if a better one is found in some amount of time
    sessionState.setSuccessNotificationTaskId(Bukkit.getScheduler()
        .runTaskLater(JourneySpigot.getInstance(),
            () -> {
              player.spigot().sendMessage(Format.success("Showing a particle trail!"));
              sessionState.setSolutionPresented(true);
            },
            20 /* one second (20 ticks) */)
        .getTaskId());
  }

  @EventHandler
  public void stopSearchEvent(SpigotStopSearchEvent event) {
    PlayerSearchSession session = getPlayerSession(event);
    if (session != null) {
      // Send failure message if we finished unsuccessfully
      Player player = Bukkit.getPlayer(session.getCallerId());
      if (player != null) {
        switch (session.getState()) {
          case FAILED -> player.spigot().sendMessage(Format.error("Search ended. There is no path to your destination."));
          case CANCELED -> player.spigot().sendMessage(Format.info("Search canceled."));
          case SUCCESSFUL -> {
            /* Don't say anything. They were already notified of the successful solutions. */
          }
          default -> Bukkit.getLogger().warning("A player search session stopped while in the "
              + session.getState() + " state");
        }
        // Remove from the searching set so they can search again
        JourneySpigot.getInstance().getSearchManager().removeSearch(player.getUniqueId());
      }
    }
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {

    if (event.getTo() == null) {
      return;
    }
    LocationCell cell = new LocationCell(event.getTo());
    UUID playerUuid = event.getPlayer().getUniqueId();
    PlayerJourney playerJourney = JourneySpigot.getInstance().getSearchManager().getJourney(playerUuid);

    if (playerJourney == null) {
      // We don't care about a player moving unless there's a journey happening
      return;
    }

    LocationCell currentLocation = JourneySpigot.getInstance().getSearchManager().getLocation(playerUuid);
    if (currentLocation == null) {
      JourneySpigot.getInstance().getSearchManager().putLocation(playerUuid, cell);
      return;
    }

    if (currentLocation.equals(cell)) {
      return;
    }

    JourneySpigot.getInstance().getSearchManager().putLocation(playerUuid, cell);
    playerJourney.visit(cell);
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    // Stop the search so we don't waste memory on someone who isn't here anymore
    SearchSession<LocationCell, World> currentSearch = JourneySpigot.getInstance()
        .getSearchManager()
        .getSearch(event.getPlayer().getUniqueId());
    if (currentSearch != null) {
      currentSearch.cancel();
    }
  }

  private PlayerSearchSession getPlayerSession(SpigotSearchEvent<?> event) {
    if (event.getSearchEvent().getSession() instanceof PlayerSearchSession) {
      return (PlayerSearchSession) event.getSearchEvent().getSession();
    } else {
      return null;
    }
  }

}
