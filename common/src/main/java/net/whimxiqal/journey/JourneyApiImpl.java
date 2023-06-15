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

package net.whimxiqal.journey;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.navigation.journey.PlayerJourneySession;
import net.whimxiqal.journey.search.DestinationGoalSearchSession;
import net.whimxiqal.journey.search.SearchFlag;
import net.whimxiqal.journey.search.SearchResult;
import net.whimxiqal.journey.search.SearchResultImpl;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.search.flag.FlagSet;

public class JourneyApiImpl implements JourneyApi {

  @Override
  public void registerScope(String plugin, String id, Scope scope) {
    if (!Journey.get().proxy().schedulingManager().isMainThread()) {
      Journey.logger().warn("Plugin " + plugin + " tried to register a scope asynchronously. Please contact the plugin owner.");
      return;
    }
    Journey.get().scopeManager().register(plugin, id, scope);
  }

  @Override
  public void registerTunnels(String plugin, TunnelSupplier tunnelSupplier) {
    if (!Journey.get().proxy().schedulingManager().isMainThread()) {
      Journey.logger().warn("Plugin " + plugin + " tried to register a tunnels asynchronously. Please contact the plugin owner.");
      return;
    }
    Journey.get().tunnelManager().register(tunnelSupplier);
  }

  @Override
  public Future<SearchResult> runDestinationSearch(JourneyAgent agent, Cell origin, Cell destination, SearchFlag<?>... flags) {
    CompletableFuture<SearchResult> future = new CompletableFuture<>();
    if (Journey.get().proxy().schedulingManager().isMainThread()) {
      runDestinationSearchInternal(agent, origin, destination, future, flags);
    } else {
      Journey.get().proxy().schedulingManager().schedule(() -> runDestinationSearchInternal(agent, origin, destination, future, flags), false);
    }
    return future;
  }

  private void runDestinationSearchInternal(JourneyAgent agent, Cell origin, Cell destination, CompletableFuture<SearchResult> future, SearchFlag<?>... flags) {
    if (!Journey.get().proxy().schedulingManager().isMainThread()) {
      throw new IllegalThreadStateException();
    }
    DestinationGoalSearchSession session = new DestinationGoalSearchSession(null, SearchSession.Caller.PLUGIN, agent,
        origin, destination,
        false, false);
    session.setFlags(FlagSet.from(flags));
    session.initialize();  // sets the modes and tunnels (must be run on main thread)
    session.search().thenAccept(result -> {
      switch (result.state()) {
        case STOPPED_SUCCESSFUL -> {
          Itinerary itinerary = result.itinerary();
          if (itinerary == null) {
            Journey.logger().error("Found null itinerary from result that returned success");
            future.complete(new SearchResultImpl(SearchResult.Status.ERROR, null));
            return;
          }
          future.complete(new SearchResultImpl(SearchResult.Status.SUCCESS, itinerary.stages()
              .flatten(path -> path, path -> path)
              .stream()
              .flatMap(path -> path.getSteps().stream())
              .collect(Collectors.toUnmodifiableList())));
        }
        case STOPPED_CANCELED -> future.complete(new SearchResultImpl(SearchResult.Status.CANCELED, null));
        case STOPPED_FAILED -> future.complete(new SearchResultImpl(SearchResult.Status.FAILED, null));
        case STOPPED_ERROR -> future.complete(new SearchResultImpl(SearchResult.Status.ERROR, null));
        default -> {
          Journey.logger().error("Session completed with invalid final state: " + result.state());
          future.complete(new SearchResultImpl(SearchResult.Status.ERROR, null));
        }
      }
    });
  }

  @Override
  public Future<SearchResult> runPlayerDestinationSearch(UUID playerUuid, Cell destination, boolean display, SearchFlag<?>... flags) {
    CompletableFuture<SearchResult> future = new CompletableFuture<>();
    if (Journey.get().proxy().schedulingManager().isMainThread()) {
      runPlayerDestinationSearchInternal(playerUuid, destination, display, future, flags);
    } else {
      Journey.get().proxy().schedulingManager().schedule(() -> runPlayerDestinationSearchInternal(playerUuid, destination, display, future, flags), false);
    }
    return future;
  }

  private void runPlayerDestinationSearchInternal(UUID playerUuid, Cell destination, boolean display, CompletableFuture<SearchResult> future, SearchFlag<?>... flags) {
    if (!Journey.get().proxy().schedulingManager().isMainThread()) {
      throw new IllegalThreadStateException();
    }
    Optional<InternalJourneyPlayer> player = Journey.get().proxy().platform().onlinePlayer(playerUuid);
    if (player.isEmpty()) {
      future.complete(new SearchResultImpl(SearchResult.Status.ERROR, null));
      return;
    }
    Optional<Cell> playerLocation = player.get().location();
    if (playerLocation.isEmpty()) {
      future.complete(new SearchResultImpl(SearchResult.Status.ERROR, null));
      return;
    }

    DestinationGoalSearchSession session = new DestinationGoalSearchSession(null, SearchSession.Caller.PLUGIN, player.get(),
        playerLocation.get(), destination,
        false, false);
    session.setFlags(FlagSet.from(flags));
    session.initialize();  // sets the modes and tunnels (must be run on main thread)
    session.search().thenAccept(result -> {
      switch (result.state()) {
        case STOPPED_SUCCESSFUL -> {
          Itinerary itinerary = result.itinerary();
          if (itinerary == null) {
            Journey.logger().error("Found null itinerary from result that returned success");
            future.complete(new SearchResultImpl(SearchResult.Status.ERROR, null));
            return;
          }

          SearchResult searchResult = new SearchResultImpl(SearchResult.Status.SUCCESS, itinerary.steps()
              .stream()
              .collect(Collectors.toUnmodifiableList()));
          if (display) {
            Journey.get().proxy().schedulingManager().schedule(() -> {
              PlayerJourneySession journey = new PlayerJourneySession(session.getAgentUuid(), session, itinerary);
              // Save the journey
              Journey.get().searchManager().putJourney(session.getAgentUuid(), journey);  // cancels any ongoing ones
              // start the journey
              journey.run();
              future.complete(searchResult);
            }, false);
          } else {
            future.complete(searchResult);
          }
        }
        case STOPPED_CANCELED -> future.complete(new SearchResultImpl(SearchResult.Status.CANCELED, null));
        case STOPPED_FAILED -> future.complete(new SearchResultImpl(SearchResult.Status.FAILED, null));
        case STOPPED_ERROR -> future.complete(new SearchResultImpl(SearchResult.Status.ERROR, null));
        default -> {
          Journey.logger().error("Session completed with invalid final state: " + result.state());
          future.complete(new SearchResultImpl(SearchResult.Status.ERROR, null));
        }
      }
    });
  }

}
