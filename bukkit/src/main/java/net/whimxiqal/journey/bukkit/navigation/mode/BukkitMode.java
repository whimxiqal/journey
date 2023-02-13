/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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

package net.whimxiqal.journey.bukkit.navigation.mode;

import net.whimxiqal.journey.navigation.Mode;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.bukkit.util.BukkitUtil;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

/**
 * A general implementation of modes used in Spigot Minecraft.
 */
public abstract class BukkitMode extends Mode {

  private final Set<Material> forcePassable;

  /**
   * General constructor.
   *
   * @param session       the session
   * @param forcePassable the list of passable materials
   */
  public BukkitMode(SearchSession session, Set<Material> forcePassable) {
    super(session);
    this.forcePassable = forcePassable;
  }

  // TODO move all of these methods into a static file
  //  and possibly get it from Baritone
  protected boolean isVerticallyPassable(BlockData block) {
    return BukkitUtil.isVerticallyPassable(block, forcePassable);
  }

  protected boolean isLaterallyPassable(BlockData block) {
    return BukkitUtil.isLaterallyPassable(block, forcePassable);
  }

  protected boolean isPassable(BlockData block) {
    return BukkitUtil.isPassable(block, forcePassable);
  }

  protected boolean canStandOn(BlockData block) {
    return BukkitUtil.canStandOn(block, forcePassable);
  }

  protected boolean canStandIn(BlockData block) {
    return BukkitUtil.canStandIn(block, forcePassable);
  }

}
