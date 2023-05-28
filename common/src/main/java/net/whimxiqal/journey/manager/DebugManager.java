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

package net.whimxiqal.journey.manager;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.whimxiqal.journey.Journey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A manager for handling transient debug state.
 * This is meant to be instantiated once and provided along with the plugin instance.
 * Each player (and the console) can enable debug mode.
 */
public final class DebugManager {

  private final Map<UUID, Targeter> debuggers = new ConcurrentHashMap<>();
  private final PlainTextComponentSerializer componentSerializer = PlainTextComponentSerializer.plainText();
  private final AtomicBoolean consoleDebugging = new AtomicBoolean(false);

  /**
   * Begin debugging for a specific player.
   * Thread-safe.
   *
   * @param debugger the player doing the debugging
   * @param target   the player for which the debugger is debugging
   */
  public void startDebuggingPlayer(@NotNull UUID debugger, @NotNull UUID target) {
    this.debuggers.put(debugger, Targeter.player(target));
  }

  /**
   * Begin debugging for all players.
   * Thread-safe.
   *
   * @param debugger the player doing the debugging
   */
  public void startDebuggingAll(@NotNull UUID debugger) {
    this.debuggers.put(debugger, Targeter.all());
  }

  /**
   * Stop debugging for a player altogether, no matter who he/she was targeting.
   * Thread-safe.
   *
   * @param player the player doing the debugging
   */
  public void stopDebugging(@NotNull UUID player) {
    this.debuggers.remove(player);
  }

  /**
   * Determine whether a player is currently in debug mode.
   * Thread-safe.
   *
   * @param player the player potentially doing the debugging
   * @return true if debugging
   */
  public boolean isDebugging(@NotNull UUID player) {
    return debuggers.containsKey(player);
  }

  public boolean isConsoleDebugging() {
    return consoleDebugging.get();
  }

  public void setConsoleDebugging(boolean debug) {
    consoleDebugging.set(debug);
  }

  /**
   * Broadcast a message to everyone in debug-mode and targeting this player.
   * Thread-safe.
   *
   * @param message the message
   */
  public void broadcast(Component message, UUID cause) {
    debuggers.forEach((debugger, targeter) -> {
      Audience audience = Journey.get().proxy().audienceProvider().player(debugger);
      if (targeter.targets(cause)) {
        audience.sendMessage(message);
      }
    });
    if (consoleDebugging.get()) {
      Journey.logger().debug(componentSerializer.serialize(message));
    }
  }

  /**
   * Broadcast a message to everyone in debugging mode.
   * Thread-safe.
   *
   * @param message the message
   */
  public void broadcast(Component message) {
    debuggers.forEach((debugger, targeter) -> Journey.get().proxy().audienceProvider().player(debugger).sendMessage(message));
    if (consoleDebugging.get()) {
      Journey.get().proxy().audienceProvider().console().sendMessage(message);
    }
  }

  /**
   * A class to determine how a player is debugging: either getting debug messages for a specific player
   * or for everyone on the server.
   */
  public static class Targeter {

    @Nullable
    private final UUID target;

    private Targeter(@Nullable UUID target) {
      this.target = target;
    }

    private static Targeter player(@NotNull UUID player) {
      return new Targeter(player);
    }

    private static Targeter all() {
      return new Targeter(null);
    }

    boolean targets(@NotNull UUID playerUuid) {
      return playerUuid.equals(target);
    }

    @NotNull
    UUID requireTarget() {
      if (target == null) {
        throw new NoSuchElementException("This target targets all and therefore "
            + "an individual target cannot be retrieved.");
      }
      return target;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Targeter that = (Targeter) o;
      return Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
      return Objects.hash(target);
    }
  }

}
