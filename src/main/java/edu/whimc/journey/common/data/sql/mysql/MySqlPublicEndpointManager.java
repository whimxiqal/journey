package edu.whimc.journey.common.data.sql.mysql;

import edu.whimc.journey.common.data.sql.DataAdapter;
import edu.whimc.journey.common.data.sql.SqlPublicEndpointManager;
import edu.whimc.journey.common.navigation.Cell;

/**
 * An implementation of the public endpoint manager for SQL with the MySQL engine.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public abstract class MySqlPublicEndpointManager<T extends Cell<T, D>, D>
    extends SqlPublicEndpointManager<T, D> {

  /**
   * General constructor.
   *
   * @param dataAdapter the data adapter
   */
  public MySqlPublicEndpointManager(DataAdapter<T, D> dataAdapter) {
    super(new MySqlConnectionController(), dataAdapter);
  }

}
