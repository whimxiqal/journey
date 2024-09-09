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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import net.whimxiqal.journey.navigation.NavigationApi;
import net.whimxiqal.journey.navigation.NavigatorDetails;
import net.whimxiqal.journey.navigation.NavigationResult;
import net.whimxiqal.journey.navigation.NavigatorDetailsBuilder;
import net.whimxiqal.journey.navigation.NavigatorDetailsBuilderImpl;
import net.whimxiqal.journey.navigation.NavigatorFactory;
import net.whimxiqal.journey.navigation.TrailNavigator;
import net.whimxiqal.journey.navigation.TrailNavigatorDetailsBuilder;
import net.whimxiqal.journey.navigation.TrailNavigatorDetailsBuilderImpl;
import net.whimxiqal.journey.search.SearchStep;

public class NavigationApiImpl implements NavigationApi {
  @Override
  public void registerNavigator(NavigatorFactory navigatorFactory) {
    Journey.get().navigatorManager().registerNavigatorFactory(navigatorFactory);
  }

  @Override
  public CompletionStage<NavigationResult> navigate(JourneyAgent agent, List<? extends SearchStep> path) {
    return navigate(agent, path, NavigatorDetails.of(TrailNavigator.TRAIL_NAVIGATOR_ID));
  }

  @Override
  public CompletionStage<NavigationResult> navigate(JourneyAgent agent, List<? extends SearchStep> path, NavigatorDetails navigatorDetails) {
    if (Journey.get().proxy().schedulingManager().isMainThread()) {
      return Journey.get().navigatorManager().startNavigating(agent, path, navigatorDetails);
    } else {
      CompletableFuture<NavigationResult> future = new CompletableFuture<>();
      Journey.get().proxy().schedulingManager().schedule(() -> Journey.get()
          .navigatorManager()
          .startNavigating(agent, path, navigatorDetails)
          .thenAccept(future::complete),
          false);
      return future;
    }
  }

  @Override
  public CompletionStage<NavigationResult> navigatePlayer(UUID playerUuid, List<? extends SearchStep> path) throws NoSuchElementException {
    return navigatePlayer(playerUuid, path, NavigatorDetails.of(TrailNavigator.TRAIL_NAVIGATOR_ID));
  }

  @Override
  public CompletionStage<NavigationResult> navigatePlayer(UUID playerUuid, List<? extends SearchStep> path, NavigatorDetails navigatorDetails) throws NoSuchElementException {
    Optional<InternalJourneyPlayer> player = Journey.get().proxy().platform().onlinePlayer(playerUuid);
    if (player.isEmpty()) {
      throw new NoSuchElementException("Player " + playerUuid + " could not be found");
    }
    return navigate(player.get(), path, navigatorDetails);
  }

  @Override
  public NavigatorDetailsBuilder<?> navigatorDetailsBuilder(String navigatorType) {
    return new NavigatorDetailsBuilderImpl.Self(navigatorType);
  }

  @Override
  public TrailNavigatorDetailsBuilder<?> trailNavigatorDetailsBuilder() {
    return new TrailNavigatorDetailsBuilderImpl();
  }

  @Override
  public CompletionStage<Integer> stopNavigation(UUID agentUuid) {
    if (Journey.get().proxy().schedulingManager().isMainThread()) {
      return CompletableFuture.completedFuture(Journey.get().navigatorManager().stopNavigators(agentUuid));
    } else {
      CompletableFuture<Integer> future = new CompletableFuture<>();
      Journey.get().proxy().schedulingManager().schedule(() -> future.complete(Journey.get()
              .navigatorManager()
              .stopNavigators(agentUuid)),
          false);
      return future;
    }
  }
}
