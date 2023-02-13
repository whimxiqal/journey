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

package net.whimxiqal.journey.search.event;

import net.whimxiqal.journey.navigation.Step;
import net.whimxiqal.journey.search.PathTrial;
import net.whimxiqal.journey.search.SearchSession;

/**
 * An event dispatched when a location has been "visited" by the
 * {@link SearchSession} algorithm in the execution of a
 * {@link PathTrial}.
 * The visitation represents when a location is saved for possible later review
 * when deciding which visited block locations have the best chance of being useful
 * on the optimal path.
 *
 * @see StepSearchEvent
 * @see SearchDispatcher
 * @see PathTrial#attempt(boolean)
 */
public class VisitationSearchEvent extends SearchEvent {

  private final Step prospectiveStep;

  /**
   * General constructor.
   *
   * @param session         the session
   * @param prospectiveStep the step that would be taken if this visited block was chosen
   *                        as the best next-location to move into.
   */
  public VisitationSearchEvent(SearchSession session, Step prospectiveStep) {
    super(session);
    this.prospectiveStep = prospectiveStep;
  }

  /**
   * Get the prospective step that would be taken if this block
   * were to be chosen by the algorithm as the next-best location.
   *
   * @return the step
   */
  public Step getProspectiveStep() {
    return prospectiveStep;
  }

  @Override
  EventType type() {
    return EventType.VISITATION;
  }
}
