package edu.whimc.journey.spigot.manager;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A manager for handling transient debug state.
 * This is meant to be instantiated once and provided along with the plugin instance.
 * Each player (and the console) can enable debug mode.
 */
public class DebugManager {

  private final Map<UUID, Target> debuggers = new ConcurrentHashMap<>();
  @Setter
  @Getter
  private boolean consoleDebugging = false;

  public void startDebuggingPlayer(@NotNull Player debugger, @NotNull Player target) {
    this.debuggers.put(debugger.getUniqueId(), Target.player(target));
  }

  public void startDebuggingAll(@NotNull Player debugger) {
    this.debuggers.put(debugger.getUniqueId(), Target.all());
  }

  public void stopDebugging(@NotNull Player player) {
    this.debuggers.remove(player.getUniqueId());
  }

  @Nullable
  public Target getDebuggingTarget(@NotNull Player debugger) {
    return debuggers.get(debugger.getUniqueId());
  }

  public boolean isDebugging(@NotNull Player player) {
    return debuggers.containsKey(player.getUniqueId());
  }

  /**
   * Broadcast a message to everyone in debug-mode and targeting this player
   *
   * @param message the message
   */
  public void broadcast(BaseComponent[] message, UUID cause) {
    debuggers.forEach((debugger, target) -> {
      Player player = Bukkit.getServer().getPlayer(debugger);
      if (player != null && target.targets(cause)) {
        player.spigot().sendMessage(message);
      }
    });
    if (consoleDebugging) {
      Bukkit.getConsoleSender().spigot().sendMessage(message);
    }
  }

  /**
   * Broadcast a message to everyone in debugging mode.
   * @param message the message
   */
  public void broadcast(BaseComponent[] message) {
    debuggers.forEach((debugger, target) -> {
      Player player = Bukkit.getServer().getPlayer(debugger);
      if (player != null) {
        player.spigot().sendMessage(message);
      }
    });
    if (consoleDebugging) {
      Bukkit.getConsoleSender().spigot().sendMessage(message);
    }
  }

  public static class Target {

    @Nullable
    private final UUID target;

    private Target(@Nullable UUID target) {
      this.target = target;
    }

    private static Target player(@NotNull Player player) {
      return new Target(player.getUniqueId());
    }

    private static Target all() {
      return new Target(null);
    }

    boolean targetsAll() {
      return target == null;
    }

    boolean targets(@NotNull Player player) {
      return player.getUniqueId().equals(target);
    }

    boolean targets(@NotNull UUID playerUuid) {
      return playerUuid.equals(target);
    }

    @NotNull
    Player requireTarget() {
      if (target == null) {
        throw new NoSuchElementException("This target targets all and therefore an individual target cannot be retrieved.");
      }
      return Objects.requireNonNull(Bukkit.getPlayer(target));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Target that = (Target) o;
      return Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
      return Objects.hash(target);
    }
  }

}
