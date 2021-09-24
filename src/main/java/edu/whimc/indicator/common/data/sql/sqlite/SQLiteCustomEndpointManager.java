package edu.whimc.indicator.common.data.sql.sqlite;

import edu.whimc.indicator.common.data.sql.DataConverter;
import edu.whimc.indicator.common.data.sql.SQLCustomEndpointManager;
import edu.whimc.indicator.common.navigation.Cell;

public abstract class SQLiteCustomEndpointManager<T extends Cell<T, D>, D>
    extends SQLCustomEndpointManager<T, D> {

  /**
   * General constructor.
   */
  public SQLiteCustomEndpointManager(DataConverter<T, D> dataConverter) {
    super(new SQLiteConnectionController(), dataConverter);
  }

}
