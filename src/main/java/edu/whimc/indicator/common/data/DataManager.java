package edu.whimc.indicator.common.data;

import edu.whimc.indicator.common.path.Cell;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public interface DataManager<T extends Cell<T, D, F>, D, F extends Function<String, D> & Serializable> {

  CustomEndpoint<T, D, F> getCustomEndpointManager();

//  ServerEndpoint<T, D, F> getServerEndpointManager();

  interface CustomEndpoint<T extends Cell<T, D, F>, D, F extends Function<String, D> & Serializable> {

    /**
     * Add a player and a specific cell to the database.
     * A unique name will automatically be generated for this cell.
     *
     * @param playerUuid the player's uuid
     * @param cell       the cell to add
     * @throws IllegalArgumentException if the player and cell are not a unique combination
     */
    void addCustomEndpoint(UUID playerUuid, T cell) throws IllegalArgumentException;

    /**
     * Add a player to a specific cell to the database with a unique name.
     *
     * @param playerUuid the player's uuid
     * @param cell       the cell to add
     * @param name       the name of the location
     * @throws IllegalArgumentException if the player and cell are not a unique combination
     *                                  or if the player and name are not a unique combination
     */
    void addCustomEndpoint(UUID playerUuid, T cell, String name) throws IllegalArgumentException;

    /**
     * Remove a player and a cell from the database. Name is irrelevant
     *
     * @param playerUuid the player's uuid
     * @param cell       the cell location
     * @throws IllegalArgumentException if the player and cell cannot be found
     */
    void removeCustomEndpoint(UUID playerUuid, T cell) throws IllegalArgumentException;

    /**
     * Remove a player and a named cell from the database. Cell location is irrelevant.
     *
     * @param playerUuid the player's uuid
     * @param name       the name of the location
     * @throws IllegalArgumentException
     */
    void removeCustomEndpoint(UUID playerUuid, String name) throws IllegalArgumentException;

    /**
     * Check if a player has a custom endpoint at the certain cell location
     *
     * @param playerUuid the player's uuid
     * @param cell       the cell location
     * @return true if the cell exists for the given player
     */
    boolean hasCustomEndpoint(UUID playerUuid, T cell);

    /**
     * Check if a player has a certain named custom endpoint
     *
     * @param playerUuid the player's uuid
     * @param name       the cell name
     * @return true if the cell exists for the given player
     */
    default boolean hasCustomEndpoint(UUID playerUuid, String name) {
      return getCustomEndpoint(playerUuid, name) != null;
    }

    /**
     * Get a specific cell with a given unique player and name combination
     *
     * @param playerUuid the player's uuid
     * @param name       the cell name
     * @return the cell, or null if it does not exist
     */
    @Nullable
    T getCustomEndpoint(UUID playerUuid, String name);

    /**
     * Get a list of all custom endpoints for a player
     *
     * @param playerUuid the player's uuid
     * @return all names of cells mapped to their corresponding cells
     */
    Map<String, T> getCustomEndpoints(UUID playerUuid);

  }

}
