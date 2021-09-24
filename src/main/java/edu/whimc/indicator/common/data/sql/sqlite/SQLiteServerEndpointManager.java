package edu.whimc.indicator.common.data.sql.sqlite;

import edu.whimc.indicator.common.data.sql.DataConverter;
import edu.whimc.indicator.common.data.sql.SQLServerEndpointManager;
import edu.whimc.indicator.common.navigation.Cell;

public abstract class SQLiteServerEndpointManager<T extends Cell<T, D>, D> extends SQLServerEndpointManager<T, D> {
  public SQLiteServerEndpointManager(DataConverter<T, D> dataConverter) {
    super(new SQLiteConnectionController(), dataConverter);
  }

}