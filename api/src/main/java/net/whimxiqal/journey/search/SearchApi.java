package net.whimxiqal.journey.search;

import java.util.UUID;
import java.util.concurrent.Future;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.JourneyAgent;

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

  Future<SearchResult> runDestinationSearch(JourneyAgent agent, Cell origin, Cell destination, SearchFlags flags);

  Future<SearchResult> runPlayerDestinationSearch(UUID playerUuid, Cell destination, SearchFlags searchFlags);

}
