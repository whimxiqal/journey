package edu.whimc.indicator.spigot.cache;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DebugManager {

  private final Set<UUID> debugging = ConcurrentHashMap.newKeySet();
  @Setter
  @Getter
  private boolean consoleDebugging = false;

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
    if (consoleDebugging) {
      Bukkit.getConsoleSender().sendMessage(message);
    }
  }

  public void broadcastDebugMessage(BaseComponent[] message) {
    debugging.forEach(uuid -> {
      Player player = Bukkit.getServer().getPlayer(uuid);
      if (player != null) {
        player.spigot().sendMessage(message);
      }
    });
    if (consoleDebugging) {
      Bukkit.getConsoleSender().spigot().sendMessage(message);
    }
  }

}
