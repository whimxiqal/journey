package edu.whimc.indicator.spigot.data;

import edu.whimc.indicator.common.data.SQLiteCustomEndpointDataManager;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.util.UuidToWorld;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SpigotSQLiteCustomEndpointDataManager extends SQLiteCustomEndpointDataManager<LocationCell, World, UuidToWorld> {
  @Override
  protected @NotNull String getDomainIdentifier(@NotNull World domain) {
    return domain.getUID().toString();
  }

  @Override
  protected @NotNull LocationCell makeCell(int x, int y, int z, @NotNull String domainId) {
    return new LocationCell(x, y, z, UUID.fromString(domainId));
  }
}
