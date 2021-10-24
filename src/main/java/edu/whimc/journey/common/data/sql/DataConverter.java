package edu.whimc.journey.common.data.sql;

import edu.whimc.journey.common.navigation.Cell;
import org.jetbrains.annotations.NotNull;

public interface DataConverter<T extends Cell<T, D>, D> {

  @NotNull
  String getDomainIdentifier(@NotNull D domain);

  @NotNull
  T makeCell(int x, int y, int z, @NotNull String domainId);

}
