package edu.whimc.journey.common.data.sql.sqlite;

import edu.whimc.journey.common.data.sql.DataAdapter;
import edu.whimc.journey.common.data.sql.SqlPublicEndpointManager;
import edu.whimc.journey.common.navigation.Cell;

public abstract class SqlitePublicEndpointManager<T extends Cell<T, D>, D> extends SqlPublicEndpointManager<T, D> {
  public SqlitePublicEndpointManager(String address, DataAdapter<T, D> dataAdapter) {
    super(new SqliteConnectionController(address), dataAdapter);
  }

}