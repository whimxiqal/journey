package edu.whimc.journey.common.data;

import edu.whimc.journey.common.navigation.Cell;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PersonalEndpointManager<T extends Cell<T, D>, D> {

  /**
   * Add a player and a specific cell to the database.
   * A unique name will automatically be generated for this cell.
   *
   * @param playerUuid the player's uuid
   * @param cell       the cell to add
   * @throws IllegalArgumentException if the player and cell are not a unique combination
   */
  void addCustomEndpoint(@NotNull UUID playerUuid,
                         @NotNull T cell) throws IllegalArgumentException, DataAccessException;

  /**
   * Add a player to a specific cell to the database with a unique name.
   *
   * @param playerUuid the player's uuid
   * @param cell       the cell to add
   * @param name       the name of the location
   * @throws IllegalArgumentException if the player and cell are not a unique combination
   *                                  or if the player and name are not a unique combination
   */
  void addCustomEndpoint(@NotNull UUID playerUuid,
                         @NotNull T cell,
                         @NotNull String name) throws IllegalArgumentException, DataAccessException;

  /**
   * Remove a player and a cell from the database. Name is irrelevant.
   * Does nothing if this combination does not exist.
   *
   * @param playerUuid the player's uuid
   * @param cell       the cell location
   */
  void removeCustomEndpoint(@NotNull UUID playerUuid,
                            @NotNull T cell) throws DataAccessException;

  /**
   * Remove a player and a named cell from the database. Cell location is irrelevant.
   * Does nothing if this combination does not exist.
   *
   * @param playerUuid the player's uuid
   * @param name       the name of the location
   */
  void removeCustomEndpoint(@NotNull UUID playerUuid,
                            @NotNull String name) throws DataAccessException;

  /**
   * Check if a player has a custom endpoint at the certain cell location
   *
   * @param playerUuid the player's uuid
   * @param cell       the cell location
   * @return true if the cell exists for the given player
   */
  default boolean hasCustomEndpoint(@NotNull UUID playerUuid,
                                    @NotNull T cell) throws DataAccessException {
    return getCustomEndpointName(playerUuid, cell) != null;
  }

  /**
   * Check if a player has a certain named custom endpoint
   *
   * @param playerUuid the player's uuid
   * @param name       the cell name
   * @return true if the cell exists for the given player
   */
  default boolean hasCustomEndpoint(@NotNull UUID playerUuid,
                                    @NotNull String name) throws DataAccessException {
    return getCustomEndpoint(playerUuid, name) != null;
  }

  /**
   * Get the name of a custom location with a given unique player and cell
   *
   * @param playerUuid the player's uuid
   * @param cell       the custom location
   * @return the cell's name, or null if it doesn't exist
   */
  @Nullable
  String getCustomEndpointName(@NotNull UUID playerUuid,
                               @NotNull T cell) throws DataAccessException;

  /**
   * Get a specific cell with a given unique player and name combination
   *
   * @param playerUuid the player's uuid
   * @param name       the cell name
   * @return the cell, or null if it doesn't
   */
  @Nullable
  T getCustomEndpoint(@NotNull UUID playerUuid,
                      @NotNull String name) throws DataAccessException;

  /**
   * Get a list of all custom endpoints for a player
   *
   * @param playerUuid the player's uuid
   * @return all names of cells mapped to their corresponding cells
   */
  Map<String, T> getPersonalEndpoints(@NotNull UUID playerUuid) throws DataAccessException;

}
