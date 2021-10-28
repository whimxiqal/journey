package edu.whimc.journey.common.data.sql.mysql;

import edu.whimc.journey.common.data.sql.DataAdapter;
import edu.whimc.journey.common.data.sql.SqlPersonalEndpointManager;
import edu.whimc.journey.common.navigation.Cell;

/**
 * MySQL-implementation of the SQL personal endpoint manager.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public abstract class MySqlPersonalEndpointManager<T extends Cell<T, D>, D>
    extends SqlPersonalEndpointManager<T, D> {

  /**
   * General constructor.
   */
  public MySqlPersonalEndpointManager(DataAdapter<T, D> dataAdapter) {
    super(new MySqlConnectionController(), dataAdapter);
  }

}
