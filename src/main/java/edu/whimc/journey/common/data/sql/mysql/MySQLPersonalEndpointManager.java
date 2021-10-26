package edu.whimc.journey.common.data.sql.mysql;

import edu.whimc.journey.common.data.sql.DataAdapter;
import edu.whimc.journey.common.data.sql.SQLPersonalEndpointManager;
import edu.whimc.journey.common.navigation.Cell;
import java.util.Properties;

public abstract class MySQLPersonalEndpointManager<T extends Cell<T, D>, D> extends SQLPersonalEndpointManager<T, D> {

  private Properties databaseProperties;

  /**
   * General constructor.
   */
  public MySQLPersonalEndpointManager(DataAdapter<T, D> dataAdapter) {
    super(new MySqlConnectionController(), dataAdapter);
  }

}
