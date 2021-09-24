package edu.whimc.indicator.common.data.sql.mysql;

import edu.whimc.indicator.common.data.sql.DataConverter;
import edu.whimc.indicator.common.data.sql.SQLCustomEndpointManager;
import edu.whimc.indicator.common.navigation.Cell;

import java.util.*;

public abstract class MySQLCustomEndpointManager<T extends Cell<T, D>, D> extends SQLCustomEndpointManager<T, D> {

  private Properties databaseProperties;

  /**
   * General constructor.
   */
  public MySQLCustomEndpointManager(DataConverter<T, D> dataConverter) {
    super(new MySQLConnectionController(), dataConverter);
  }

}
