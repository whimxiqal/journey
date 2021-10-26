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

package edu.whimc.journey.common.search;

import edu.whimc.journey.common.navigation.Cell;
import edu.whimc.journey.common.navigation.Port;
import edu.whimc.journey.common.navigation.Mode;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A session to handle a pathfinding search.
 * Every time a person wants to find an {@link edu.whimc.journey.common.navigation.Itinerary}
 * from some origin to some destination, they can run a search from this session.
 *
 * @param <T> the cell type
 * @param <D> the domain type
 */
public abstract class SearchSession<T extends Cell<T, D>, D> implements Resulted {

  protected final List<Port<T, D>> ports = new LinkedList<>();
  protected final List<Mode<T, D>> modes = new LinkedList<>();
  private final UUID callerId;
  private final UUID uuid = UUID.randomUUID();
  private final Caller callerType;
  private int algorithmStepDelay = 0;

  protected ResultState state = ResultState.IDLE;

  protected SearchSession(UUID callerId, Caller callerType) {
    this.callerId = callerId;
    this.callerType = callerType;
  }

  /**
   * Perform the titular pathfinding search.
   * Uses the local tracker to report stages of the operation.
   *
   * @param origin      the starting location of the pathfinding search
   * @param destination the ending location of the pathfinding search
   */
  public abstract void search(T origin, T destination);

  /**
   * Terminate the search operation. It is up to the implementation
   * of this search session object to implement the actual cancellation behavior;
   * this method only tells the class that it should be canceling itself.
   */
  public final void cancel() {
    if (!state.hasFinished()) {
      this.state = ResultState.CANCELING;
    }
  }

  @Override
  public final ResultState getState() {
    return state;
  }

  public final void registerPort(Port<T, D> port) {
    this.ports.add(port);
  }

  public final void registerMode(Mode<T, D> mode) {
    this.modes.add(mode);
  }

  public final UUID getCallerId() {
    return callerId;
  }

  public final UUID getUuid() {
    return uuid;
  }

  public Caller getCallerType() {
    return callerType;
  }

  protected void setAlgorithmStepDelay(int delay) {
    this.algorithmStepDelay = delay;
  }

  public int getAlgorithmStepDelay() {
    return algorithmStepDelay;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchSession<?, ?> that = (SearchSession<?, ?>) o;
    return uuid.equals(that.uuid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid);
  }

  public enum Caller {
    PLAYER,
    OTHER
  }
}
