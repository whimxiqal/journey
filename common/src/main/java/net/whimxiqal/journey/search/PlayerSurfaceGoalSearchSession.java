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

package net.whimxiqal.journey.search;

import java.util.UUID;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.search.flag.FlagSet;
import net.whimxiqal.journey.search.flag.Flags;
import net.kyori.adventure.audience.Audience;

public class PlayerSurfaceGoalSearchSession extends LocalUpwardsGoalSearchSession implements PlayerSessionStateful {

  private final PlayerSessionState sessionState;

  public PlayerSurfaceGoalSearchSession(UUID player, Cell origin, FlagSet flags) {
    super(player, SearchSession.Caller.PLAYER, origin, flags);
    sessionState = new PlayerSessionState(player);
    int stepDelay = flags.getValueFor(Flags.ANIMATE);
    if (stepDelay > 0) {
      sessionState.animationManager().setAnimating(true);
      setAlgorithmStepDelay(stepDelay);
    } else {
      sessionState.animationManager().setAnimating(false);
    }

    // We don't need any tunnels for this!
    Journey.get().proxy().platform().prepareSearchSession(this, player, flags, false);
  }

  public PlayerSessionState sessionState() {
    return sessionState;
  }

  @Override
  public boolean reachesGoal(Cell cell) {
    return Journey.get().proxy().platform().isAtSurface(cell);
  }

  @Override
  public Audience audience() {
    return Journey.get().proxy().audienceProvider().player(getCallerId());
  }
}
