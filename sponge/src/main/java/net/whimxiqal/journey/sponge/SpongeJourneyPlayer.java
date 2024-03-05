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

package net.whimxiqal.journey.sponge;

import java.util.Optional;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.InternalJourneyPlayer;
import net.whimxiqal.journey.sponge.util.SpongeUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.Slot;

public class SpongeJourneyPlayer extends InternalJourneyPlayer {

  public SpongeJourneyPlayer(ServerPlayer player) {
    super(player.uniqueId(), player.name());
  }

  @Override
  public Optional<Cell> location() {
    return Sponge.server().player(uuid).map(player -> SpongeUtil.toCell(player.serverLocation()));
  }

  @Override
  public boolean canFly() {
    Optional<ServerPlayer> player = Sponge.server().player(uuid);
    if (player.isEmpty()) {
      // player is outdated
      return false;
    }
    return player.get().canFly().get();
  }

  @Override
  public boolean hasBoat() {
    Optional<ServerPlayer> player = Sponge.server().player(uuid);
    if (player.isEmpty()) {
      // player is outdated
      return false;
    }
    for (Slot slot : player.get().inventory().slots()) {
      if (slot.peek().type().get(Keys.BOAT_TYPE).isPresent()) {
        return true;
      }
    }
    return false;
  }
}
