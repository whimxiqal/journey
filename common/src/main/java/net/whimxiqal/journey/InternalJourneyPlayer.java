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

package net.whimxiqal.journey;

import java.util.Optional;
import java.util.UUID;
import net.whimxiqal.mantle.common.CommandSource;
import net.whimxiqal.mantle.common.Mantle;

/**
 * A {@link JourneyPlayer} with extra information for internal Journey purposes,
 * not to be exposed to the API.
 *
 * This is not a thread-safe object, so all calls must be on the main thread.
 */
public abstract class InternalJourneyPlayer implements JourneyPlayer {

  protected final UUID uuid;
  protected final String name;

  public InternalJourneyPlayer(UUID uuid, String name) {
    this.uuid = uuid;
    this.name = name;
  }

  public static InternalJourneyPlayer from(CommandSource source) {
    if (source.type() != CommandSource.Type.PLAYER) {
      throw new IllegalArgumentException("Can only create a JourneyPlayer from a player type CommandSource");
    }
    Optional<InternalJourneyPlayer> optional = Journey.get().proxy().platform().onlinePlayer(source.uuid());
    if (optional.isEmpty()) {
      throw new IllegalStateException("Player " + source.uuid() + " cannot be found");
    }
    return optional.get();
  }

  @Override
  public UUID uuid() {
    return uuid;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean hasPermission(String permission) {
    // Piggyback off Mantle
    return Mantle.getProxy().hasPermission(uuid, permission);
  }

  @Override
  public String toString() {
    return name + " (" + uuid + ")";
  }

  /**
   * Can the player fly?
   *
   * @return yes if flight is enabled
   */
  public abstract boolean canFly();

  /**
   * Does the player have a boat?
   *
   * @return yes if the player has a boat
   */
  public abstract boolean hasBoat();
}
