package edu.whimc.indicator.spigot.data;

import edu.whimc.indicator.common.data.sql.DataConverter;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SpigotDataConverter implements DataConverter<LocationCell, World> {

  @Override
  @NotNull
  public String getDomainIdentifier(@NotNull World domain) {
    return domain.getUID().toString();
  }

  @Override
  @NotNull
  public LocationCell makeCell(int x, int y, int z, @NotNull String domainId) {
    return new LocationCell(x, y, z, UUID.fromString(domainId));
  }

}
