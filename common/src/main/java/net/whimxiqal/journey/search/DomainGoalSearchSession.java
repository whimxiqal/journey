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

package net.whimxiqal.journey.search;

import java.util.UUID;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.JourneyPlayer;

public class DomainGoalSearchSession extends GraphGoalSearchSession<DomainSearchGraph> {
  protected final int domain;

  public DomainGoalSearchSession(UUID callerId, Caller callerType, JourneyAgent agent, Cell origin, int destinationDomain, boolean persistentOrigin) {
    super(callerId, callerType, agent, origin, persistentOrigin);
    this.domain = destinationDomain;
  }

  public DomainGoalSearchSession(JourneyPlayer player, Cell origin, int destinationDomain, boolean persistentOrigin) {
    this(player.uuid(), Caller.PLAYER, player, origin, destinationDomain, persistentOrigin);
  }

  @Override
  public void asyncSearch() {
    // Do an initial check to make sure we're not in the given domain, then run the normal execution
    if (domain == origin.domain()) {
      state.set(ResultState.STOPPED_ERROR);
      complete(null);
      return;
    }
    super.asyncSearch();
  }

  @Override
  DomainSearchGraph createSearchGraph() {
    return new DomainSearchGraph(this, origin, domain);
  }

}
