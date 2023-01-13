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

package net.whimxiqal.journey.common.search;

import java.util.UUID;
import net.whimxiqal.journey.common.Journey;
import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.journey.common.search.flag.FlagSet;
import net.whimxiqal.journey.common.search.flag.Flags;
import net.kyori.adventure.audience.Audience;

/**
 * A search session designed to be used for players finding their way to a specific destination.
 */
public class PlayerDestinationGoalSearchSession extends DestinationGoalSearchSession implements PlayerSessionStateful {

  private final PlayerSessionState sessionState;

  public PlayerDestinationGoalSearchSession(UUID player, Cell origin, Cell destination, FlagSet flags, boolean persistentDestination) {
    super(player, Caller.PLAYER, flags, origin, destination, false, persistentDestination);
    sessionState = new PlayerSessionState(player);
    if (flags.hasFlag(Flags.ANIMATE)) {
      sessionState.animationManager().setAnimating(true);
      setAlgorithmStepDelay(flags.valueOf(Flags.ANIMATE));
    } else {
      sessionState.animationManager().setAnimating(false);
    }
    Journey.get().proxy().platform().prepareSearchSession(this, player, flags, true);
    Journey.get().proxy().platform().prepareDestinationSearchSession(this, player, flags, destination);
    Journey.get().netherManager().makePorts().forEach(this::registerPort);
  }

  public PlayerSessionState sessionState() {
    return sessionState;
  }

  @Override
  public Audience audience() {
    return Journey.get().proxy().audienceProvider().player(getCallerId());
  }
}
