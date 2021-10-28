package edu.whimc.journey.common.data.sql;

import edu.whimc.journey.common.navigation.Cell;
import org.jetbrains.annotations.NotNull;

/**
 * An adapter for the data types put into storage.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public interface DataAdapter<T extends Cell<T, D>, D> {

  /**
   * Get the string identifier for a domain.
   *
   * @param domain the domain
   * @return the string identifier
   */
  @NotNull
  String getDomainIdentifier(@NotNull D domain);

  /**
   * Create a cell using the data required to create one:
   * three coordinates and a domain identifier.
   *
   * @param x        the x coordinate
   * @param y        the y coordinate
   * @param z        the z coordinate
   * @param domainId the domain identifier
   * @return a new cell
   */
  @NotNull
  T makeCell(int x, int y, int z, @NotNull String domainId);

}
