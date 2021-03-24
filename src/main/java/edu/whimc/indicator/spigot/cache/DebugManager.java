package edu.whimc.indicator.spigot.cache;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DebugManager {

  private final Set<UUID> debugging = ConcurrentHashMap.newKeySet();

  @SuppressWarnings("UnusedReturnValue")
  public boolean startDebugging(UUID playerUuid) {
    return debugging.add(playerUuid);
  }

  @SuppressWarnings("UnusedReturnValue")
  public boolean stopDebugging(UUID playerUuid) {
    return debugging.remove(playerUuid);
  }

  public boolean isDebugging(UUID playerUuid) {
    return debugging.contains(playerUuid);
  }

  public void broadcastDebugMessage(String message) {
    debugging.forEach(uuid -> {
      Player player = Bukkit.getServer().getPlayer(uuid);
      if (player != null) {
        player.sendMessage(message);
      }
    });
  }

}
