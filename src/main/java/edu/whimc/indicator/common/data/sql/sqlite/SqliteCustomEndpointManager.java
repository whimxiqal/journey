package edu.whimc.indicator.common.data.sql.sqlite;

import edu.whimc.indicator.common.data.sql.DataConverter;
import edu.whimc.indicator.common.data.sql.SQLCustomEndpointManager;
import edu.whimc.indicator.common.navigation.Cell;

public abstract class SqliteCustomEndpointManager<T extends Cell<T, D>, D>
    extends SQLCustomEndpointManager<T, D> {

  /**
   * General constructor.
   */
  public SqliteCustomEndpointManager(String address, DataConverter<T, D> dataConverter) {
    super(new SqliteConnectionController(address), dataConverter);
  }

}
