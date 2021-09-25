package edu.whimc.indicator.common.data.sql;

import edu.whimc.indicator.common.data.CustomEndpointManager;
import edu.whimc.indicator.common.data.DataAccessException;
import edu.whimc.indicator.common.navigation.Cell;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SQLCustomEndpointManager<T extends Cell<T, D>, D>
    extends SQLEndpointManager<T, D>
    implements CustomEndpointManager<T, D> {

  public SQLCustomEndpointManager(SQLConnectionController connectionController, DataConverter<T, D> dataConverter) {
    super(connectionController, dataConverter);
  }

  @Override
  public void addCustomEndpoint(@NotNull UUID playerUuid, @NotNull T cell) throws IllegalArgumentException, DataAccessException {
    this.addEndpoint(playerUuid, cell);
  }

  @Override
  public void addCustomEndpoint(@NotNull UUID playerUuid, @NotNull T cell, @NotNull String name) throws IllegalArgumentException, DataAccessException {
    this.addEndpoint(playerUuid, cell, name);
  }

  @Override
  public void removeCustomEndpoint(@NotNull UUID playerUuid, @NotNull T cell) throws DataAccessException {
    this.removeEndpoint(playerUuid, cell);
  }

  @Override
  public void removeCustomEndpoint(@NotNull UUID playerUuid, @NotNull String name) throws DataAccessException {
    this.removeEndpoint(playerUuid, name);
  }

  @Override
  public @Nullable String getCustomEndpointName(@NotNull UUID playerUuid, @NotNull T cell) throws DataAccessException {
    return this.getEndpointName(playerUuid, cell);
  }

  @Override
  public @Nullable T getCustomEndpoint(@NotNull UUID playerUuid, @NotNull String name) throws DataAccessException {
    return this.getEndpoint(playerUuid, name);
  }

  @Override
  public Map<String, T> getCustomEndpoints(@NotNull UUID playerUuid) throws DataAccessException {
    return this.getEndpoints(playerUuid);
  }
}
