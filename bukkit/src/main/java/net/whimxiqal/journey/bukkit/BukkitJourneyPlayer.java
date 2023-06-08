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

import java.util.Optional;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.InternalJourneyPlayer;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import net.whimxiqal.journey.bukkit.util.MaterialGroups;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitJourneyPlayer extends InternalJourneyPlayer {

  public BukkitJourneyPlayer(Player player) {
    super(player.getUniqueId(), player.getName());
  }

  @Override
  public Optional<Cell> location() {
    return Optional.ofNullable(Bukkit.getPlayer(uuid)).map(player -> BukkitUtil.cell(player.getLocation()));
  }

  @Override
  public boolean canFly() {
    Player player = Bukkit.getPlayer(uuid);
    if (player == null) {
      // player is outdated
      return false;
    }
    return player.getAllowFlight();
  }

  @Override
  public boolean hasBoat() {
    Player player = Bukkit.getPlayer(uuid);
    if (player == null) {
      // player is outdated
      return false;
    }
    return MaterialGroups.BOATS.stream().anyMatch(boatType -> player.getInventory().contains(boatType));
  }
}
