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

package me.pietelite.journey.common.search;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.navigation.Cell;
import me.pietelite.journey.common.navigation.ModeType;

/**
 * The place where all animation operations and memory are stored for any single
 * {@link PlayerDestinationGoalSearchSession}.
 */
public class AnimationManager {

  private final UUID player;
  private final Set<Cell> successfulLocations = ConcurrentHashMap.newKeySet();
  private Cell lastFailure;
  private boolean animating;

  public enum StageType {
    FAILURE,
    SUCCESS,
    STEP
  }

  public AnimationManager(UUID player) {
    this.player = player;
  }

  /**
   * Show the result of an algorithm step to the user.
   *
   * @param cell     the location
   * @param success  whether the step was a successful move
   * @param modeType the mode used to get there
   * @return true if it showed the result correctly, false if it failed for some reason
   */
  public boolean showResult(Cell cell, boolean success, ModeType modeType) {
    if (!animating) {
      return false;
    }

    if (this.successfulLocations.contains(cell)) {
      return false;
    }
    if (Journey.get()
        .proxy()
        .platform()
        .sendBlockData(player, cell, success ? StageType.SUCCESS : StageType.FAILURE, modeType)) {
      successfulLocations.add(cell);
      if (!success) {
        if (lastFailure != null) {
          Journey.get().proxy().platform().resetBlockData(player, Collections.singleton(lastFailure));
        }
        lastFailure = cell;
      }
      return true;
    }
    return false;
  }

  private void hideResult(Cell cell) {
    Journey.get().proxy().platform().resetBlockData(player, Collections.singleton(cell));
    successfulLocations.remove(cell);
  }

  /**
   * Show the location of a step in an algorithm to the user.
   *
   * @param cell the location
   * @return true if it displayed correctly, false if it failed for some reason
   */
  public boolean showStep(Cell cell) {
    if (Journey.get()
        .proxy()
        .platform()
        .sendBlockData(player, cell, StageType.STEP, null)) {
      successfulLocations.add(cell);
      return true;
    }
    return false;
  }

  /**
   * Undo all the block changes displayed to the user so everything looks as it did before animating.
   */
  public void undoAnimation() {
    Journey.get().proxy().platform().resetBlockData(player, successfulLocations);
    if (lastFailure != null) {
      Journey.get().proxy().platform().resetBlockData(player, Collections.singleton(lastFailure));
    }
  }

  /**
   * Whether this manager should be creating new animations.
   * When set to false, the animations will still be reset and
   * cleaned up normally.
   *
   * @return true if animating
   */
  public boolean isAnimating() {
    return animating;
  }

  /**
   * Set whether this manager should be creating new animations.
   * When set to false, the animations will still be reset and
   * cleaned up normally.
   *
   * @param animating true to animate
   */
  public void setAnimating(boolean animating) {
    this.animating = animating;
  }
}
