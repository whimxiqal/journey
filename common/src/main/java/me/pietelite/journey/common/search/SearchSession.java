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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import me.pietelite.journey.common.Journey;
import me.pietelite.journey.common.message.Formatter;
import me.pietelite.journey.common.navigation.Itinerary;
import me.pietelite.journey.common.navigation.Mode;
import me.pietelite.journey.common.navigation.ModeType;
import me.pietelite.journey.common.navigation.Port;
import me.pietelite.journey.common.search.flag.FlagSet;
import net.kyori.adventure.audience.Audience;

/**
 * A session to handle a pathfinding search.
 * Every time a person wants to find an {@link Itinerary}
 * from some origin to some destination, they can run a search from this session.
 *
 * <p>This class must be thread-safe because it will be called asynchronously
 */
public abstract class SearchSession implements Resulted {

  protected final List<Port> ports = new LinkedList<>();
  protected final List<Mode> modes = new LinkedList<>();
  private final UUID callerId;
  private final UUID uuid = UUID.randomUUID();
  private final Caller callerType;
  private final FlagSet flags;
  protected AtomicReference<ResultState> state = new AtomicReference<>(ResultState.IDLE);
  protected AtomicReference<CompletableFuture<ResultState>> future = new AtomicReference<>(new CompletableFuture<>());
  private int algorithmStepDelay = 0;

  protected SearchSession(UUID callerId, Caller callerType, FlagSet flags) {
    this.callerId = callerId;
    this.callerType = callerType;
    this.flags = flags;
  }

  /**
   * Perform the titular search operation.
   */
  public final CompletableFuture<ResultState> search(int timeout) {
    Journey.get().proxy().schedulingManager().schedule(() -> {
      try {
        doSearch();
        if (this.getState() == ResultState.CANCELING_FAILED) {
          audience().sendMessage(Formatter.error("Time limit surpassed. Canceling search..."));
        }
      } catch (Exception e) {
        if (callerType == Caller.PLAYER) {
          Journey.get().proxy().audienceProvider().player(callerId).sendMessage(Formatter.error("A problem occurred with your search. Please notify an administrator"));
        }
        state.set(ResultState.STOPPED_ERROR);
        e.printStackTrace();
      }
      future.get().complete(state.get());
    }, true);

    // Set up cancellation task
    Journey.get().proxy().schedulingManager().schedule(this::stop, false, timeout * 20 /* ticks per second */);
    return future.get();
  }

  protected abstract void doSearch() throws SearchException;

  public final AtomicReference<CompletableFuture<ResultState>> future() {
    return future;
  }

  /**
   * Terminate the search operation. It is up to the implementation
   * of this search session object to implement the actual cancellation behavior;
   * this method only tells the class that it should be canceling itself.
   */
  public final void stop() {
    state.getAndUpdate(current -> {
      switch (current) {
        case IDLE:
        case RUNNING:
          return ResultState.CANCELING_FAILED;
        case RUNNING_SUCCESSFUL:
          return ResultState.CANCELING_SUCCESSFUL;
        default:
          return current;
      }
    });
  }

  @Override
  public final ResultState getState() {
    return state.get();
  }

  /**
   * Register a {@link Port} to use in the pathfinding system.
   * The ports are what allow the algorithm to jump directly around the different worlds,
   * so all possible avenues to do so should be registered before running the search.
   *
   * @param port the port
   */
  public final void registerPort(Port port) {
    this.ports.add(port);
  }

  /**
   * Register a {@link Mode} of transportation in the pathfinding system.
   * Modes are how the algorithm determines whether the moving entity can
   * get from place to adjacent place.
   *
   * @param mode the mode
   */
  public final void registerMode(Mode mode) {
    this.modes.add(mode);
  }

  /**
   * Get a copy of all the registered ports on this session.
   *
   * @return the ports
   */
  public final Collection<Port> ports() {
    List<Port> copy = new ArrayList<>(ports.size());
    copy.addAll(ports);
    return copy;
  }

  /**
   * Get a copy of all the registered modes on this session.
   *
   * @return the modes
   */
  public final Collection<Mode> modes() {
    List<Mode> copy = new ArrayList<>(modes.size());
    copy.addAll(modes);
    return copy;
  }

  /**
   * Get a set of all mode types that are registered on this session.
   *
   * @return the mode types
   */
  public final Set<ModeType> modeTypes() {
    return modes.stream().map(Mode::type).collect(Collectors.toSet());
  }

  /**
   * Get the identifier of the caller, for indexing and retrieval purposes.
   *
   * @return the id
   */
  public final UUID getCallerId() {
    return callerId;
  }

  /**
   * Get the unique identifier of this session.
   *
   * @return the id
   */
  public final UUID uuid() {
    return uuid;
  }

  /**
   * Get the type of caller, for classification purposes.
   *
   * @return the type of caller.
   */
  public Caller getCallerType() {
    return callerType;
  }

  public FlagSet flags() {
    return flags;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchSession that = (SearchSession) o;
    return uuid.equals(that.uuid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid);
  }

  /**
   * Get the time since the search operation execution, in milliseconds.
   * It will return a negative number if the method is called before the search operation.
   *
   * @return the search execution time, in milliseconds
   */
  public abstract long executionTime();

  public abstract Audience audience();

  public void setAlgorithmStepDelay(int algorithmStepDelay) {
    this.algorithmStepDelay = algorithmStepDelay;
  }

  public int getAlgorithmStepDelay() {
    return algorithmStepDelay;
  }

  /**
   * The caller type. A search session may be created for multiple types of entities,
   * but generally they are players.
   * In some scenarios, we may want an NPC of some sort to follow the path,
   * so in that case, the caller would be something other than Player.
   */
  public enum Caller {
    PLAYER,
    OTHER
  }
}
