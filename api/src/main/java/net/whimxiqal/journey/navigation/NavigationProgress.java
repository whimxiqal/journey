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

import java.util.List;
import net.whimxiqal.journey.Synchronous;
import net.whimxiqal.journey.search.SearchStep;

/**
 * A source of information about an {@link net.whimxiqal.journey.JourneyAgent}'s progress
 * while completing a {@link Navigator}.
 */
public interface NavigationProgress {

  /**
   * The steps in the path the agent must traverse.
   *
   * @return the steps
   */
  @Synchronous
  List<? extends SearchStep> steps();

  /**
   * The index in the list of steps of the path that corresponds to the {@link SearchStep}
   * that the agent is currently traversing.
   *
   * @return the index of the current step
   */
  @Synchronous
  int currentStepIndex();

  /**
   * The portion of the step that has been traversed so far, from [0, 1). So, if the
   * agent has traversed halfway from the destination of the previous step to the
   * destination of the current step, the progress would be 0.5.
   *
   * @return the progress made on the current step
   */
  @Synchronous
  double currentStepProgress();

}
