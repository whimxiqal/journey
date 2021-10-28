package edu.whimc.journey.spigot.manager;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * A manager for handling transient debug state.
 * This is meant to be instantiated once and provided along with the plugin instance.
 * Each player (and the console) can enable debug mode.
 */
public class DebugManager {

  private final Set<UUID> debugging = ConcurrentHashMap.newKeySet();
  @Setter
  @Getter
  private boolean consoleDebugging = false;

  /**
   * Enable debugging for a player.
   *
   * @param playerUuid the uuid of the player
   */
  public void startDebugging(UUID playerUuid) {
    debugging.add(playerUuid);
  }

  /**
   * Disable debugging for a player.
   *
   * @param playerUuid the uuid of the player
   */
  public void stopDebugging(UUID playerUuid) {
    debugging.remove(playerUuid);
  }

  /**
   * Determine whether a player is in debug mode.
   *
   * @param playerUuid the uuid of the player
   * @return true if debugging
   */
  public boolean isDebugging(UUID playerUuid) {
    return debugging.contains(playerUuid);
  }

  /**
   * Broadcast a message to everyone in debugging mode.
   *
   * @param message the message
   */
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
