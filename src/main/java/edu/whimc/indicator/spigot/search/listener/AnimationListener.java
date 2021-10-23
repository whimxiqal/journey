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

package edu.whimc.indicator.spigot.search.listener;

import edu.whimc.indicator.common.navigation.Cell;
import edu.whimc.indicator.spigot.search.AnimationManager;
import edu.whimc.indicator.spigot.search.PlayerSearchSession;
import edu.whimc.indicator.spigot.search.event.SpigotModeSuccessEvent;
import edu.whimc.indicator.spigot.search.event.SpigotSearchEvent;
import edu.whimc.indicator.spigot.search.event.SpigotStepSearchEvent;
import edu.whimc.indicator.spigot.search.event.SpigotStopPathSearchEvent;
import edu.whimc.indicator.spigot.search.event.SpigotStopSearchEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * A class to listen for Spigot events originating from
 * {@link edu.whimc.indicator.common.search.SearchSession#search(Cell, Cell)}
 * and allowing for the animating of the algorithm in realtime.
 */
public class AnimationListener implements Listener {

  @EventHandler
  public void stepSearchEvent(SpigotStepSearchEvent event) {
    AnimationManager manager = getAnimationManager(event);
    if (manager != null) {
      manager.showBlock(event.getSearchEvent().getStep().getLocatable(), Material.OBSIDIAN.createBlockData());
    }
  }

  @EventHandler
  public void stopPathSearchEvent(SpigotStopPathSearchEvent event) {
    AnimationManager manager = getAnimationManager(event);
    if (manager != null) {
      manager.cleanUpAnimation();
    }
  }

  @EventHandler
  public void stopSearchEvent(SpigotStopSearchEvent event) {
    AnimationManager manager = getAnimationManager(event);
    if (manager != null) {
      manager.cleanUpAnimation();
    }
  }

  private AnimationManager getAnimationManager(SpigotSearchEvent<?> event) {
    if (event.getSearchEvent().getSession() instanceof PlayerSearchSession) {
      AnimationManager animationManager = ((PlayerSearchSession) event.getSearchEvent().getSession())
          .getAnimationManager();
      if (animationManager.isAnimating()) {
        return animationManager;
      }
    }
    return null;
  }

}
