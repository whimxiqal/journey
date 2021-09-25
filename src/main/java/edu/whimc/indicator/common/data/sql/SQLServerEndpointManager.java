package edu.whimc.indicator.common.data.sql;

import edu.whimc.indicator.common.data.DataAccessException;
import edu.whimc.indicator.common.data.ServerEndpointManager;
import edu.whimc.indicator.common.navigation.Cell;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SQLServerEndpointManager<T extends Cell<T, D>, D>
    extends SQLEndpointManager<T, D>
    implements ServerEndpointManager<T, D> {

  public SQLServerEndpointManager(SQLConnectionController connectionController, DataConverter<T, D> dataConverter) {
    super(connectionController, dataConverter);
  }

  @Override
  public void addServerEndpoint(@NotNull T cell, @NotNull String name) throws IllegalArgumentException, DataAccessException {
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
  public Map<String, T> getServerEndpoints() throws DataAccessException {
    return getEndpoints(null);
  }
}
