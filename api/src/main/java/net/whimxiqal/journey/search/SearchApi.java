package net.whimxiqal.journey.search;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.JourneyAgent;

/**
 * A sub-API to be used for managing searching within Journey.
 */
public interface SearchApi {

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
  @Deprecated(since = "1.2.0")
  Future<SearchResult> runDestinationSearch(JourneyAgent agent, Cell origin, Cell destination,
                                            SearchFlag<?>... flags);

  /**
   * Execute a search from a given origin to a given destination for a given {@link JourneyAgent}.
   * Flags may be specified to alter the behavior of the search.
   *
   * <p>The resulting completion stage will always be completed on the main thread
   *
   * @param agent       the agent
   * @param origin      the origin of the search
   * @param destination the destination of the search
   * @param flags       the flags to alter the behavior of the search
   * @return a completion stage with the result of the search, for callbacks
   */
  CompletionStage<SearchResult> runDestinationSearch(JourneyAgent agent,
                                                     Cell origin, Cell destination,
                                                     SearchFlags flags);

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
  @Deprecated(since = "1.2.0")
  Future<SearchResult> runPlayerDestinationSearch(UUID playerUuid, Cell destination,
                                                  boolean display, SearchFlag<?>... flags);

  /**
   * Execute a search from a given origin to a given destination for a player with the given UUID.
   * The origin of the search will be the player's current location.
   * Flags may be specified to alter the behavior of the search.
   *
   * <p>The resulting completion stage will always be completed on the main thread
   *
   * @param playerUuid  the uuid of the player
   * @param destination the destination of the search
   * @param flags       the flags to alter the behavior of the search
   * @return a completion stage with the result of the search, for callbacks
   */
  CompletionStage<SearchResult> runPlayerDestinationSearch(UUID playerUuid, Cell destination,
                                                           SearchFlags flags);

}
