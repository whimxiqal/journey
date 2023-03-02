/*
 * MIT License
 *
 * Copyright (c) whimxiqal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
