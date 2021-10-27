package edu.whimc.journey.common.data.sql;

import edu.whimc.journey.common.data.DataAccessException;
import edu.whimc.journey.common.data.PublicEndpointManager;
import edu.whimc.journey.common.navigation.Cell;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A public endpoint manager implemented for SQL.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public abstract class SqlPublicEndpointManager<T extends Cell<T, D>, D>
    extends SqlEndpointManager<T, D>
    implements PublicEndpointManager<T, D> {

  /**
   * General constructor.
   *
   * @param connectionController the connection controller
   * @param dataAdapter          the data adapter
   */
  public SqlPublicEndpointManager(SqlConnectionController connectionController,
                                  DataAdapter<T, D> dataAdapter) {
    super(connectionController, dataAdapter);
  }

  @Override
  public void addServerEndpoint(@NotNull T cell, @NotNull String name)
      throws IllegalArgumentException, DataAccessException {
    addEndpoint(null, cell, name);
  }

  @Override
  public void removeServerEndpoint(@NotNull T cell) throws DataAccessException {
    removeEndpoint(null, cell);
  }

  @Override
  public void removeServerEndpoint(@NotNull String name) throws DataAccessException {
    removeEndpoint(null, name);
  }

  @Override
  public @Nullable String getServerEndpointName(@NotNull T cell) throws DataAccessException {
    return getEndpointName(null, cell);
  }

  @Override
  public @Nullable T getServerEndpoint(@NotNull String name) throws DataAccessException {
    return getEndpoint(null, name);
  }

  @Override
  public Map<String, T> getPublicEndpoints() throws DataAccessException {
    return getEndpoints(null);
  }
}
