package edu.whimc.indicator.common.data.sql.mysql;

import edu.whimc.indicator.common.data.sql.DataConverter;
import edu.whimc.indicator.common.data.sql.SQLServerEndpointManager;
import edu.whimc.indicator.common.navigation.Cell;

public abstract class MySQLServerEndpointManager<T extends Cell<T, D>, D> extends SQLServerEndpointManager<T, D> {
  public MySQLServerEndpointManager(DataConverter<T, D> dataConverter) {
    super(new MySQLConnectionController(), dataConverter);
  }

}
