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

package net.whimxiqal.journey.navigation;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.Synchronous;
import net.whimxiqal.journey.search.SearchStep;

/**
 * A sub-API to be used for managing navigation within Journey.
 */
public interface NavigationApi {

  /**
   * Register a new navigator type using a {@link NavigatorFactory}.
   *
   * @param navigatorFactory the factory to register
   */
  @Synchronous
  void registerNavigator(NavigatorFactory navigatorFactory);

  /**
   * Navigate a {@link JourneyAgent} along a path of {@link SearchStep}s using default
   * navigation information.
   *
   * <p>The resultant completion stage will always complete on the main server thread.
   *
   * @param agent the agent
   * @param path  the path
   * @return the navigation result in a completion stage, for callbacks
   */
  CompletionStage<NavigationResult> navigate(JourneyAgent agent,
                                             List<? extends SearchStep> path);

  /**
   * Navigate a {@link JourneyAgent} along a path of {@link SearchStep}s using the
   * specified navigator details to determine what kind of navigator will be used.
   *
   * <p>The resultant completion stage will always complete on the main server thread.
   *
   * @param agent            the agent
   * @param path             the path
   * @param navigatorDetails the navigator details
   * @return the navigation result in a completion stage, for callbacks
   */
  CompletionStage<NavigationResult> navigate(JourneyAgent agent,
                                             List<? extends SearchStep> path,
                                             NavigatorDetails navigatorDetails);

  /**
   * Navigate a player along a path of {@link SearchStep}s using default
   * navigation information.
   *
   * <p>The resultant completion stage will always complete on the main server thread.
   *
   * @param playerUuid the player's uuid
   * @param path       the path
   * @return the navigation result in a completion stage, for callbacks
   * @throws IllegalStateException if the player cannot be found
   */
  CompletionStage<NavigationResult> navigatePlayer(UUID playerUuid,
                                                   List<? extends SearchStep> path)
      throws NoSuchElementException;

  /**
   * Navigate a {@link JourneyAgent} along a path of {@link SearchStep}s using the
   * specified navigator details to determine what kind of navigator will be used.
   *
   * <p>The resultant completion stage will always complete on the main server thread.
   *
   * @param playerUuid       the player's uuid
   * @param path             the path
   * @param navigatorDetails the navigator details
   * @return the navigation result in a completion stage, for callbacks
   * @throws IllegalStateException if the player cannot be found
   */
  CompletionStage<NavigationResult> navigatePlayer(UUID playerUuid,
                                                   List<? extends SearchStep> path,
                                                   NavigatorDetails navigatorDetails)
      throws NoSuchElementException;

  /**
   * Create a generic builder for {@link NavigatorDetails} for the given navigator type.
   *
   * @param navigatorType the navigator type
   * @return the builder
   */
  NavigatorDetailsBuilder<?> navigatorDetailsBuilder(String navigatorType);

  /**
   * Create a builder for {@link NavigatorDetails} with details for a "trail navigator".
   *
   * @return the builder
   */
  TrailNavigatorDetailsBuilder<?> trailNavigatorDetailsBuilder();

  /**
   * Stop all navigation for the agent (or player) with the given uuid
   *
   * <p>The resultant completion stage will always complete on the main server thread.
   *
   * @param agentUuid the agent's uuid
   * @return the number of navigators stopped in a completion stage, for callbacks
   */
  CompletionStage<Integer> stopNavigation(UUID agentUuid) throws NoSuchElementException;

}
