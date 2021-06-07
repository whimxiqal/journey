package edu.whimc.indicator.common.data;

import edu.whimc.indicator.common.path.Cell;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ServerEndpointManager<T extends Cell<T, D>, D> {

  /**
   * Add a server endpoint
   *
   * @param cell the cell to add
   * @param name the name of the location
   * @throws IllegalArgumentException for invalid inputs
   */
  void addServerEndpoint(@NotNull T cell, @NotNull String name) throws IllegalArgumentException, DataAccessException;

  /**
   * Remove a cell. Name is irrelevant. Does nothing if cell isn't saved.
   *
   * @param cell the cell location
   */
  void removeServerEndpoint(@NotNull T cell) throws DataAccessException;

  /**
   * Remove a cell from the database by name. Cell location is irrelevant.
   * Does nothing if the name doesn't exist
   *
   * @param name the name of the location
   */
  void removeServerEndpoint(@NotNull String name) throws DataAccessException;

  /**
   * Check if a saved cell exists at this location.
   *
   * @param cell the cell location
   * @return true if the cell exists
   */
  default boolean hasServerEndpoint(@NotNull T cell) throws DataAccessException {
    return getServerEndpointName(cell) != null;
  }

  /**
   * Check if a saved cell exists with this name.
   *
   * @param name the cell name
   * @return true if the cell exists
   */
  default boolean hasServerEndpoint(@NotNull String name) throws DataAccessException {
    return getServerEndpoint(name) != null;
  }

  /**
   * Get the name of a saved location.
   *
   * @param cell the saved location
   * @return the cell's name, or null if it doesn't exist
   */
  @Nullable
  String getServerEndpointName(@NotNull T cell) throws DataAccessException;

  /**
   * Get a specific cell by its given name.
   *
   * @param name the cell name
   * @return the cell, or null if it doesn't
   */
  @Nullable
  T getServerEndpoint(@NotNull String name) throws DataAccessException;

  /**
   * Get a list of all saved endpoints
   *
   * @return all names of cells mapped to their corresponding cells
   */
  Map<String, T> getServerEndpoints() throws DataAccessException;

}
