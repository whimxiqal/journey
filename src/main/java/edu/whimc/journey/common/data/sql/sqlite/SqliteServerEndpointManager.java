package edu.whimc.journey.common.data.sql.sqlite;

import edu.whimc.journey.common.data.sql.DataConverter;
import edu.whimc.journey.common.data.sql.SQLServerEndpointManager;
import edu.whimc.journey.common.navigation.Cell;

public abstract class SqliteServerEndpointManager<T extends Cell<T, D>, D> extends SQLServerEndpointManager<T, D> {
  public SqliteServerEndpointManager(String address, DataConverter<T, D> dataConverter) {
    super(new SqliteConnectionController(address), dataConverter);
  }

}