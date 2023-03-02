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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.Describable;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.message.Formatter;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.navigation.Mode;
import net.whimxiqal.journey.navigation.ModeType;
import net.whimxiqal.journey.search.event.StopSearchEvent;
import net.whimxiqal.journey.search.flag.FlagSet;
import net.whimxiqal.journey.util.SimpleTimer;

/**
 * A session to handle a pathfinding search.
 * Every time a person wants to find an {@link Itinerary}
 * from some origin to some destination, they can run a search from this session.
 *
 * <p>This class must be thread-safe because it will be called asynchronously
 */
public abstract class SearchSession implements Resulted, Describable {

  protected final List<Tunnel> tunnels = new LinkedList<>();
  protected final List<Mode> modes = new LinkedList<>();
  private final UUID callerId;
  private final UUID uuid = UUID.randomUUID();
  private final Caller callerType;
  private final List<String> permissions = new LinkedList<>();
  protected FlagSet flags = new FlagSet();
  protected ResultState state = ResultState.IDLE;  // multithreaded access, must be synchronized
  protected CompletableFuture<ResultState> future = new CompletableFuture<>();
  private Component name = Component.empty();
  private Component description = Component.empty();
  private int algorithmStepDelay = 0;
  protected final SimpleTimer timer = new SimpleTimer();

  protected SearchSession(UUID callerId, Caller callerType) {
    this.callerId = callerId;
    this.callerType = callerType;
  }

  /**
   * Perform the titular search operation.
   */
  public final CompletableFuture<ResultState> search(int timeout) {
    // kick off the first portion
    doSearch();
    // Set up cancellation task
    if (timeout > 0) {
      Journey.get().proxy().schedulingManager().schedule(() -> stop(false), false, timeout * 20 /* ticks per second */);
    }
    return future;
  }

  private void doSearch() throws SearchException {
    synchronized (this) {
      if (state.isStopped()) {
        future.complete(state);
        return;
      }
    }
    Journey.get().proxy().schedulingManager().schedule(() -> {
      try {
        if (this.getState() == ResultState.STOPPING_FAILED) {
          audience().sendMessage(Formatter.error("Time limit surpassed. Cancelling search..."));
        }
        resumeSearch();
      } catch (Exception e) {
        if (callerType == Caller.PLAYER) {
          Journey.get().proxy().audienceProvider().player(callerId).sendMessage(Formatter.error("A problem occurred with your search. Please notify an administrator"));
        }
        synchronized (this) {
          state = ResultState.STOPPED_ERROR;
        }
        e.printStackTrace();
      }
      doSearch();
    }, true);
  }

  /**
   * Resume the search by running a portion of the search algorithm but quitting before spending too long.
   * The purpose of this method is to split up the execution of searches into multiple tasks so many
   * concurrent searches may make progress even with limited async threads.
   *
   * @throws SearchException search exception
   */
  protected abstract void resumeSearch() throws SearchException;

  public final CompletableFuture<ResultState> future() {
    return future;
  }

  /**
   * Set state as stopped and send dispatch for stopped event. This method should be idempotent.
   */
  public synchronized void markStopped() {
    ResultState previousState = state;
    state = state.stoppedResult();
    if (state != previousState) {
      Journey.get().dispatcher().dispatch(new StopSearchEvent(this));
    }
  }

  /**
   * Terminate the search operation. It is up to the implementation
   * of this search session object to implement the actual cancellation behavior;
   * this method only tells the class that it should be canceling itself.
   *
   * @param cancel whether the search is stopped due to a cancellation request by a user, or not
   */
  public synchronized final void stop(boolean cancel) {
    state = state.stoppingResult(cancel);
  }

  @Override
  public synchronized final ResultState getState() {
    return state;
  }

  /**
   * Register a {@link Tunnel} to use in the pathfinding system.
   * The tunnels are what allow the algorithm to jump directly around the different worlds,
   * so all possible avenues to do so should be registered before running the search.
   *
   * @param tunnel the tunnel
   */
  public final void registerTunnel(Tunnel tunnel) {
    this.tunnels.add(tunnel);
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
   * Get a copy of all the registered tunnels on this session.
   *
   * @return the tunnels
   */
  public final Collection<Tunnel> tunnels() {
    List<Tunnel> copy = new ArrayList<>(tunnels.size());
    copy.addAll(tunnels);
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

  public void setFlags(FlagSet flags) {
    this.flags = flags;
  }

  public FlagSet flags() {
    return flags;
  }

  public void initialize() {
    // do nothing by default
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
  public final long executionTime() {
    return timer.elapsed();
  }

  public abstract Audience audience();

  public int getAlgorithmStepDelay() {
    return algorithmStepDelay;
  }

  public void setAlgorithmStepDelay(int algorithmStepDelay) {
    this.algorithmStepDelay = algorithmStepDelay;
  }

  public void setName(Component name) {
    this.name = name;
  }

  @Override
  public Component name() {
    return name;
  }

  public void setDescription(Component description) {
    this.description = description;
  }

  @Override
  public Component description() {
    return description;
  }

  public void addPermission(String permission) {
    this.permissions.add(permission);
  }

  public List<String> permissions() {
    return permissions;
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
