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

package edu.whimc.indicator.spigot.search.listener;

import edu.whimc.indicator.spigot.IndicatorSpigot;
import edu.whimc.indicator.common.navigation.Itinerary;
import edu.whimc.indicator.spigot.journey.PlayerJourney;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import edu.whimc.indicator.spigot.search.PlayerSearchSession;
import edu.whimc.indicator.spigot.search.SessionInfo;
import edu.whimc.indicator.spigot.search.event.SpigotFoundSolutionEvent;
import edu.whimc.indicator.spigot.search.event.SpigotSearchEvent;
import edu.whimc.indicator.spigot.search.event.SpigotStartSearchEvent;
import edu.whimc.indicator.spigot.search.event.SpigotStopSearchEvent;
import edu.whimc.indicator.spigot.util.Format;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SearchListener implements Listener {

  @EventHandler
  public void startSearchEvent(SpigotStartSearchEvent event) {
    PlayerSearchSession session = getPlayerSession(event);
    if (session != null) {
      Player player = Bukkit.getPlayer(event.getSearchEvent().getSession().getCallerId());
      if (player != null) {
        IndicatorSpigot.getInstance().getSearchManager().putSearch(player.getUniqueId(), session);
      }
    }
  }

  @EventHandler
  public void foundSolutionEvent(SpigotFoundSolutionEvent event) {
    PlayerSearchSession session = getPlayerSession(event);
    if (session == null) {
      return;
    }
    SessionInfo sessionInfo = session.getSessionInfo();

    Player player = Bukkit.getPlayer(session.getCallerId());
    if (player == null) {
      return;
    }

    Itinerary<LocationCell, World> itinerary = event.getSearchEvent().getItinerary();
    if (sessionInfo.wasSolutionPresented()) {
      PlayerJourney journey = IndicatorSpigot.getInstance().getSearchManager().getPlayerJourney(player.getUniqueId());
      if (journey != null) {
        journey.setProspectiveItinerary(itinerary);
        player.spigot().sendMessage(Format.info("A faster itinerary to your destination was found from your original location"));
        player.spigot().sendMessage(Format.chain(Format.info("Run "),
            Format.command("/trail accept", "Accept an incoming trail request"),
            Format.textOf(" to accept")));
        return;
      }
    }

    if (sessionInfo.wasSolved()) {
      Bukkit.getScheduler().cancelTask(sessionInfo.getSuccessNotificationTaskId());
    }
    sessionInfo.setSolved(true);

    // Create a journey that is completed when the player reaches within 3 blocks of the endpoint
    PlayerJourney journey = new PlayerJourney(player.getUniqueId(), session, itinerary);
    journey.illuminateTrail();

    // Save the journey
    IndicatorSpigot.getInstance().getSearchManager().putPlayerJourney(player.getUniqueId(), journey);

    // Set up a success notification that will be cancelled if a better one is found in some amount of time
    sessionInfo.setSuccessNotificationTaskId(Bukkit.getScheduler()
        .runTaskLater(IndicatorSpigot.getInstance(),
            () -> {
              player.spigot().sendMessage(Format.success("Showing an itinerary to your destination"));
              sessionInfo.setSolutionPresented(true);
            },
            20 /* one second (20 ticks) */)
        .getTaskId());
  }

  @EventHandler
  public void stopSearchEvent(SpigotStopSearchEvent event) {
    PlayerSearchSession session = getPlayerSession(event);
    if (session != null) {
      session.handleStop();
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
