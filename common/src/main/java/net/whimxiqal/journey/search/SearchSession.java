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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.Describable;
import net.whimxiqal.journey.InternalJourneyPlayer;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.message.Formatter;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.navigation.Mode;
import net.whimxiqal.journey.navigation.ModeType;
import net.whimxiqal.journey.navigation.mode.BoatMode;
import net.whimxiqal.journey.navigation.mode.ClimbMode;
import net.whimxiqal.journey.navigation.mode.DigMode;
import net.whimxiqal.journey.navigation.mode.DoorMode;
import net.whimxiqal.journey.navigation.mode.FlyMode;
import net.whimxiqal.journey.navigation.mode.JumpMode;
import net.whimxiqal.journey.navigation.mode.SwimMode;
import net.whimxiqal.journey.navigation.mode.WalkMode;
import net.whimxiqal.journey.search.flag.FlagSet;
import net.whimxiqal.journey.search.flag.Flags;
import net.whimxiqal.journey.util.SimpleTimer;
import org.jetbrains.annotations.Nullable;

/**
 * A session to handle a pathfinding search.
 * Every time a person wants to find an {@link Itinerary}
 * from some origin to some destination, they can run a search from this session.
 *
 * <p>This class must be thread-safe because it will be called asynchronously
 */
public abstract class SearchSession implements Describable {

  protected final SimpleTimer timer = new SimpleTimer();
  protected final UUID callerId;
  protected final UUID uuid = UUID.randomUUID();
  protected final Caller callerType;
  private final AtomicReference<List<Tunnel>> tunnels = new AtomicReference<>(Collections.emptyList());
  private final AtomicReference<List<Mode>> modes = new AtomicReference<>(Collections.emptyList());
  private final List<String> permissions = new LinkedList<>();
  protected final FlagSet flags = new FlagSet();
  protected AtomicReference<ResultState> state = new AtomicReference<>(ResultState.IDLE);
  protected CompletableFuture<Result> future = new CompletableFuture<>();
  private Component name = Component.empty();
  private List<Component> description = Collections.emptyList();

  protected SearchSession(UUID callerId, Caller callerType) {
    this.callerId = callerId;
    this.callerType = callerType;
  }

  protected final void setPlayerModes() {
    boolean fly = false;
    boolean boat = false;
    List<Mode> modes = new LinkedList<>();

    Optional<InternalJourneyPlayer> player = Journey.get().proxy().platform().onlinePlayer(callerId);
    if (player.isPresent()) {
      fly = flags.getValueFor(Flags.FLY) && player.get().canFly();
      boat = player.get().hasBoat();
    }

    if (fly) {
      modes.add(new FlyMode());
    } else {
      modes.add(new WalkMode());
      modes.add(new JumpMode());
      modes.add(new SwimMode());
      if (boat) {
        modes.add(new BoatMode());
      }
    }
    modes.add(new DoorMode());
    modes.add(new ClimbMode());
    if (flags.getValueFor(Flags.DIG)) {
      modes.add(new DigMode());
    }
    setModes(modes);
  }

  protected final void setPlayerTunnels() {
    Journey.get().proxy().platform().onlinePlayer(getCallerId()).ifPresent(jPlayer ->
        setTunnels(Journey.get().tunnelManager().tunnels(jPlayer)));
  }

  /**
   * Perform the titular search operation.
   */
  public final CompletableFuture<Result> search(int timeout) {
    Journey.logger().debug(this + ": scheduling search");
    // kick off the first portion
    Journey.get().proxy().schedulingManager().schedule(this::asyncSearch, true);
    // Set up cancellation task
    if (timeout > 0) {
      Journey.get().proxy().schedulingManager().schedule(() -> stop(false), false, timeout * 20 /* ticks per second */);
    }
    return future;
  }

  /**
   * Begin asynchronous portion of search.
   */
  protected abstract void asyncSearch();

  protected boolean evaluateState() {
    ResultState updated = state.updateAndGet(current -> {
      if (current.isStopping()) {
        return current.stoppedResult();
      } else {
        return current;
      }
    });
    return updated.isStopped();
  }

  protected final void complete(@Nullable Itinerary itinerary) {
    timer.stop();
    future.complete(new Result(state.get(), itinerary));
  }

  public record Result(ResultState state, @Nullable Itinerary itinerary) {
  }

  public final CompletableFuture<Result> future() {
    return future;
  }

  /**
   * Terminate the search operation. It is up to the implementation
   * of this search session object to implement the actual cancellation behavior;
   * this method only tells the class that it should be canceling itself.
   *
   * @param cancel whether the search is stopped due to a cancellation request by a user, or not
   */
  public synchronized final void stop(boolean cancel) {
    state.getAndUpdate(current -> current.stoppingResult(cancel));
  }

  public synchronized final ResultState getState() {
    return state.get();
  }

  /**
   * Register {@link Tunnel}s to use in the pathfinding system.
   * The tunnels are what allow the algorithm to jump directly around the different worlds,
   * so all possible avenues to do so should be registered before running the search.
   */
  public final void setTunnels(List<Tunnel> tunnels) {
    this.tunnels.set(Collections.unmodifiableList(tunnels));
  }

  /**
   * Register {@link Mode}s of transportation in the pathfinding system.
   * Modes are how the algorithm determines whether the moving entity can
   * get from place to adjacent place.
   *
   * @param mode the mode
   */
  public final void setModes(List<Mode> mode) {
    this.modes.set(Collections.unmodifiableList(mode));
  }

  public void addMode(Mode mode) {
    this.modes.updateAndGet(curModes -> {
      List<Mode> newModes = new ArrayList<>(curModes.size() + 1);
      newModes.addAll(curModes);
      newModes.add(mode);
      return Collections.unmodifiableList(newModes);
    });
  }


  /**
   * Get an immutable list of all the registered tunnels on this session.
   *
   * @return the tunnels
   */
  public final List<Tunnel> tunnels() {
    return tunnels.get();
  }

  /**
   * Get an immutable list of all the registered modes on this session.
   *
   * @return the modes
   */
  public final Collection<Mode> modes() {
    return modes.get();
  }

  /**
   * Get a set of all mode types that are registered on this session.
   *
   * @return the mode types
   */
  public final Set<ModeType> modeTypes() {
    return modes.get().stream().map(Mode::type).collect(Collectors.toSet());
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

  public void setFlags(FlagSet other) {
    this.flags.addFlags(other);
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
   * Thread-safe.
   *
   * @return the search execution time, in milliseconds
   */
  public final long executionTime() {
    return timer.elapsed();
  }

  public void setName(Component name) {
    this.name = name;
  }

  @Override
  public Component name() {
    return name;
  }

  public void setDescription(Component... description) {
    this.description = Arrays.asList(description);
  }

  public void setDescription(List<Component> description) {
    this.description = description;
  }

  @Override
  public List<Component> description() {
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
    CONSOLE,
    OTHER
  }
}
