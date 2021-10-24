package edu.whimc.journey.common.data.sql.mysql;

import edu.whimc.journey.common.data.sql.DataConverter;
import edu.whimc.journey.common.data.sql.SQLServerEndpointManager;
import edu.whimc.journey.common.navigation.Cell;

public abstract class MySQLServerEndpointManager<T extends Cell<T, D>, D> extends SQLServerEndpointManager<T, D> {
  public MySQLServerEndpointManager(DataConverter<T, D> dataConverter) {
    super(new MySqlConnectionController(), dataConverter);
  }

}
