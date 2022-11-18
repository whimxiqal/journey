package me.pietelite.journey.common.navigation;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.pietelite.journey.common.math.Vector;
import me.pietelite.journey.common.search.AnimationManager;
import me.pietelite.journey.common.search.SearchSession;
import me.pietelite.journey.common.search.flag.FlagSet;

public interface PlatformProxy {

  boolean isNetherPortal(Cell cell);

  void playSuccess(UUID playerUuid);

  void spawnDestinationParticle(UUID playerUuid, String domainId, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ);

  void spawnModeParticle(UUID playerUuid, ModeType type, String domainId, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ);

  Optional<UUID> onlinePlayer(String name);

  Optional<Cell> entityCellLocation(UUID entityUuid);

  Optional<Vector> entityVector(UUID entityUuid);

  void prepareSearchSession(SearchSession searchSession, UUID player, FlagSet flags, boolean includePorts);

  void prepareDestinationSearchSession(SearchSession searchSession, UUID player, FlagSet flags, Cell destination);

  boolean isAtSurface(Cell cell);

  boolean sendBlockData(UUID player, Cell location, AnimationManager.StageType stage, ModeType mode);

  boolean resetBlockData(UUID player, Collection<Cell> locations);
}
