package edu.whimc.journey.common.data.sql.sqlite;

import edu.whimc.journey.common.data.sql.DataAdapter;
import edu.whimc.journey.common.data.sql.SqlPersonalEndpointManager;
import edu.whimc.journey.common.navigation.Cell;

public abstract class SqlitePersonalEndpointManager<T extends Cell<T, D>, D>
    extends SqlPersonalEndpointManager<T, D> {

  /**
   * General constructor.
   */
  public SqlitePersonalEndpointManager(String address, DataAdapter<T, D> dataAdapter) {
    super(new SqliteConnectionController(address), dataAdapter);
  }

}
