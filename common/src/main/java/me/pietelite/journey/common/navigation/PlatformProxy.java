package me.pietelite.journey.common.navigation;

import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public interface PlatformProxy {

  boolean isNetherPortal(Cell cell);

  void playSuccess(UUID playerUuid);

  void spawnDestinationParticle(String domainId, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ);

  void spawnModeParticle(ModeType type, String domainId, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ);

  Optional<UUID> onlinePlayer(String name);
}
