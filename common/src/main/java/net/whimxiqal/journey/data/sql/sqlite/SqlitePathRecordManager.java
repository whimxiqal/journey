package net.whimxiqal.journey.data.sql.sqlite;

import java.util.Set;
import net.whimxiqal.journey.data.DataAccessException;
import net.whimxiqal.journey.data.sql.SqlConnectionController;
import net.whimxiqal.journey.data.sql.SqlPathRecordManager;
import net.whimxiqal.journey.search.DestinationPathTrial;
import net.whimxiqal.journey.search.ModeType;

public class SqlitePathRecordManager extends SqlPathRecordManager {
  /**
   * General constructor.
   *
   * @param connectionController the connection controller
   */
  public SqlitePathRecordManager(SqlConnectionController connectionController) {
    super(connectionController);
  }

  @Override
  public void report(DestinationPathTrial trial, Set<ModeType> modeTypes, long executionTime) throws DataAccessException {
    // do nothing
  }
}
