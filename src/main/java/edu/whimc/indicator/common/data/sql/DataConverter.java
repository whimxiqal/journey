package edu.whimc.indicator.common.data.sql;

import edu.whimc.indicator.common.navigation.Cell;
import org.jetbrains.annotations.NotNull;

public interface DataConverter<T extends Cell<T, D>, D> {

  @NotNull
  String getDomainIdentifier(@NotNull D domain);

  @NotNull
  T makeCell(int x, int y, int z, @NotNull String domainId);

}
