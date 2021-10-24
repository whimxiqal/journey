package edu.whimc.journey.common.data.sql.mysql;

import edu.whimc.journey.common.data.sql.DataConverter;
import edu.whimc.journey.common.data.sql.SQLCustomEndpointManager;
import edu.whimc.journey.common.navigation.Cell;
import java.util.Properties;

public abstract class MySQLCustomEndpointManager<T extends Cell<T, D>, D> extends SQLCustomEndpointManager<T, D> {

  private Properties databaseProperties;

  /**
   * General constructor.
   */
  public MySQLCustomEndpointManager(DataConverter<T, D> dataConverter) {
    super(new MySqlConnectionController(), dataConverter);
  }

}
