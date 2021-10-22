/*
 * Copyright 2021 Pieter Svenson
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

package edu.whimc.indicator.spigot.search;

import edu.whimc.indicator.common.navigation.ModeType;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class AnimationManager {

  private final Set<LocationCell> successfulLocations = ConcurrentHashMap.newKeySet();
  private final PlayerSearchSession session;
  private LocationCell lastFailure;
  private boolean animating;

  public AnimationManager(PlayerSearchSession session) {
    this.session = session;
  }

  public boolean showResult(LocationCell cell, boolean success, ModeType modeType) {
    Player player = Bukkit.getPlayer(session.getCallerId());
    if (player == null) {
      return false;
    }

    if (this.successfulLocations.contains(cell)) {
      return false;
    }

    if (player.getLocation().equals(cell.getBlock().getLocation())
        || player.getLocation().add(0, 1, 0).equals(cell.getBlock().getLocation())) {
      return false;
    }

    if (success) {
      if (showBlock(cell, Material.LIME_STAINED_GLASS.createBlockData())) {
        this.successfulLocations.add(cell);
        return true;
      }
    } else {
      if (lastFailure != null) {
        if (!this.successfulLocations.contains(lastFailure)) {
          hideResult(lastFailure);
        }
      }
      lastFailure = cell;
      return showBlock(cell, Material.GLOWSTONE.createBlockData());
    }
    return false;
  }

  private void hideResult(LocationCell cell) {
    Player player = Bukkit.getPlayer(session.getCallerId());
    if (player == null) {
      return;
    }
    player.sendBlockChange(cell.getBlock().getLocation(), cell.getBlock().getBlockData());
    successfulLocations.remove(cell);
  }

  public boolean showBlock(LocationCell cell, BlockData blockData) {
    Player player = getPlayer();
    if (cell.getDomain() != player.getWorld() || cell.distanceToSquared(new LocationCell(player.getLocation())) > 10000) {
      return false;
    }
    player.sendBlockChange(cell.getBlock().getLocation(), blockData);
    return true;
  }

  public void cleanUpAnimation() {
    Player player = Bukkit.getPlayer(session.getCallerId());
    if (player == null) {
      return;
    }

    successfulLocations.forEach(cell -> showBlock(cell, cell.getBlock().getBlockData()));

    if (lastFailure != null) {
      showBlock(lastFailure, lastFailure.getBlock().getBlockData());
    }
  }

  private Player getPlayer() {
    return Bukkit.getPlayer(session.getCallerId());
  }

  public void setAnimating(boolean animating) {
    this.animating = animating;
  }

  public boolean isAnimating() {
    return animating;
  }
}
