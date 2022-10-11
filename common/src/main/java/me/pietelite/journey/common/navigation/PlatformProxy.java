package me.pietelite.journey.common.navigation;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import me.pietelite.journey.common.search.AnimationManager;
import me.pietelite.journey.common.search.SearchSession;
import me.pietelite.journey.common.search.flag.FlagSet;

public interface PlatformProxy {

  boolean isNetherPortal(Cell cell);

  void playSuccess(UUID playerUuid);

  void spawnDestinationParticle(String domainId, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ);

  void spawnModeParticle(ModeType type, String domainId, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ);

  Optional<UUID> onlinePlayer(String name);

  Cell entityLocation(UUID entityUuid);

  void prepareSearchSession(SearchSession searchSession, UUID player, FlagSet flags, boolean includePorts);

  boolean isAtSurface(Cell cell);

  boolean sendBlockData(UUID player, Cell location, AnimationManager.StageType stage, ModeType mode);

  boolean resetBlockData(UUID player, Collection<Cell> locations);
}
