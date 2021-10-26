package edu.whimc.journey.common.data.sql.mysql;

import edu.whimc.journey.common.data.sql.DataAdapter;
import edu.whimc.journey.common.data.sql.SqlPublicEndpointManager;
import edu.whimc.journey.common.navigation.Cell;

public abstract class MySqlPublicEndpointManager<T extends Cell<T, D>, D> extends SqlPublicEndpointManager<T, D> {
  public MySqlPublicEndpointManager(DataAdapter<T, D> dataAdapter) {
    super(new MySqlConnectionController(), dataAdapter);
  }

}
