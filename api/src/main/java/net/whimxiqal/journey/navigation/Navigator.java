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

package net.whimxiqal.journey.navigation;

import java.util.Collection;
import java.util.Collections;
import net.whimxiqal.journey.Synchronous;

/**
 * Manage information about the traversal of locatables
 * within the game.
 */
public interface Navigator {

  /**
   * Begin navigation. This will only be called once.
   */
  @Synchronous
  boolean start();

  /**
   * Whether this navigator should be stopped or not. This is called frequently.
   *
   * @return true if navigator should stop
   */
  boolean shouldStop();

  /**
   * Stop navigation.
   * This will only be called once and after {@link #start()} is called.
   */
  void stop();

  /**
   * A collection of ids of the plugins that this navigator depend on.
   * This is used so that navigators may be stopped before their dependencies become unavailable.
   *
   * @return the plugin dependency ids
   */
  default Collection<String> pluginDependencies() {
    return Collections.emptyList();
  }

}
