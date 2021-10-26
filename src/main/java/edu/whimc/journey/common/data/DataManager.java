package edu.whimc.journey.common.data;

import edu.whimc.journey.common.navigation.Cell;

/**
 * An interface for describing what is needed to store the state for this application.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public interface DataManager<T extends Cell<T, D>, D> {

  /**
   * Get the implementation for the endpoint manager
   * specifically for personal endpoints in the search algorithm.
   *
   * @return the personal endpoint manager
   */
  PersonalEndpointManager<T, D> getPersonalEndpointManager();

  /**
   * Get the implementation for the endpoint manager
   * specifically for public endpoints in the search algorithm.
   *
   * @return the public endpoint manager
   */
  PublicEndpointManager<T, D> getPublicEndpointManager();

}
