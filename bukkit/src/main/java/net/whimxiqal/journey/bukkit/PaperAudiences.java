package net.whimxiqal.journey.bukkit;

import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PaperAudiences implements AudienceProvider {

  private final JavaPlugin plugin;

  PaperAudiences(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public @NotNull Audience all() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NotNull Audience console() {
    return Bukkit.getConsoleSender();
  }

  @Override
  public @NotNull Audience players() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NotNull Audience player(@NotNull UUID playerId) {
    return Objects.requireNonNull(Bukkit.getPlayer(playerId));
  }

  @Override
  public @NotNull Audience permission(@NotNull String permission) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NotNull Audience world(@NotNull Key world) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NotNull Audience server(@NotNull String serverName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NotNull ComponentFlattener flattener() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    // ignore
  }
}
