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

import java.util.UUID;
import java.util.concurrent.Future;
import net.whimxiqal.journey.search.SearchFlag;
import net.whimxiqal.journey.search.SearchResult;

/**
 * The central interface for all external-facing endpoints for Journey.
 *
 * <p><b>Note: unless otherwise specified, all API calls must be done on the main server thread.</b>
 */
public interface JourneyApi {

  /**
   * Register a {@link Scope} to Journey. {@link Scope}s will provide players with the information about
   * other plugin's custom destinations.
   *
   * @param id    the id of the scope. Each character must either be alphanumeric, a hyphen, or a space.
   * @param scope the scope
   */
  @Synchronous
  void registerScope(String plugin, String id, Scope scope);

  /**
   * Register a supplier of {@link Tunnel}s, indicating unique methods of travel through which
   * players may get to locations unnaturally.
   *
   * @param tunnelSupplier the supplier of tunnels for a given player
   */
  @Synchronous
  void registerTunnels(String plugin, TunnelSupplier tunnelSupplier);

  /**
   * Execute a search from a given origin to a given destination.
   * The given {@link JourneyAgent} is the entity for which the search is calculating.
   * A player is an example of a {@link JourneyAgent}.
   * To run a destination search for a player, however, it's best to use {@link #runPlayerDestinationSearch}.
   * <b>This may be called asynchronously</b>, but note that most operations on the {@link JourneyAgent} are
   * only called on the main thread.
   *
   * @param agent       the agent that is expected to traverse the path resulting from the search
   * @param origin      the origin of the search, usually the agent's current location
   * @param destination the destination
   * @param flags       optional flags to adjust the behavior of the search
   * @return a future of the result of the search
   */
  Future<SearchResult> runDestinationSearch(JourneyAgent agent, Cell origin, Cell destination,
                                            SearchFlag<?>... flags);

  /**
   * Execute a search for a player to a given destination.
   * The result may optionally be displayed to the player in the same fashion
   * it is presented in Journey by standard commands.
   * <b>This may be called asynchronously.</b>
   *
   * @param playerUuid  the uuid of the player
   * @param destination the destination
   * @param display     true if the player should be shown the resulting path
   * @param flags       optional flags to adjust the behavior of the search
   * @return a future of the result of the search
   */
  Future<SearchResult> runPlayerDestinationSearch(UUID playerUuid, Cell destination,
                                                  boolean display, SearchFlag<?>... flags);

}
