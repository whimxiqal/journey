package edu.whimc.journey.common.data.sql.sqlite;

import edu.whimc.journey.common.data.sql.DataAdapter;
import edu.whimc.journey.common.data.sql.SqlPersonalEndpointManager;
import edu.whimc.journey.common.navigation.Cell;

/**
 * The SQLite implementation of the personal endpoint manager.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public abstract class SqlitePersonalEndpointManager<T extends Cell<T, D>, D>
    extends SqlPersonalEndpointManager<T, D> {

  /**
   * General constructor.
   *
   * @param address     the address to the SQLite database.
   * @param dataAdapter the data adapter to allow proper storage of data
   */
  public SqlitePersonalEndpointManager(String address, DataAdapter<T, D> dataAdapter) {
    super(new SqliteConnectionController(address), dataAdapter);
  }

}
