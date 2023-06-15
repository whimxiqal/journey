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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.Describable;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.JourneyAgent;
import net.whimxiqal.journey.JourneyPlayer;
import net.whimxiqal.journey.Synchronous;
import net.whimxiqal.journey.Tunnel;
import net.whimxiqal.journey.navigation.Itinerary;
import net.whimxiqal.journey.navigation.Mode;
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
  @Nullable
  protected final UUID callerId;
  protected final UUID uuid = UUID.randomUUID();
  protected final Caller callerType;
  protected final JourneyAgent agent;
  protected final FlagSet flags = new FlagSet();
  private final AtomicReference<List<Tunnel>> tunnels = new AtomicReference<>(Collections.emptyList());
  private final AtomicReference<List<Mode>> modes = new AtomicReference<>(Collections.emptyList());
  private final List<String> permissions = new LinkedList<>();
  protected AtomicReference<ResultState> state = new AtomicReference<>(ResultState.IDLE);
  protected CompletableFuture<Result> future = new CompletableFuture<>();
  private Component name = Component.empty();
  private List<Component> description = Collections.emptyList();

  protected SearchSession(@Nullable UUID callerId, Caller callerType, JourneyAgent agent) {
    this.callerId = callerId;
    this.callerType = callerType;
    this.agent = agent;
  }

  protected SearchSession(JourneyPlayer player) {
    this.callerId = player.uuid();
    this.callerType = Caller.PLAYER;
    this.agent = player;
  }

  /**
   * Build a standard {@link Mode} from a {@link ModeType}.
   *
   * @param modeType the type of mode
   * @return the mode, or null if no mode is mapped to the mode type
   */
  public static Mode buildMode(ModeType modeType) {
    return switch (modeType) {
      case NONE, TUNNEL -> null;
      case WALK -> new WalkMode();
      case JUMP -> new JumpMode();
      case SWIM -> new SwimMode();
      case FLY -> new FlyMode();
      case BOAT -> new BoatMode();
      case DOOR -> new DoorMode();
      case CLIMB -> new ClimbMode();
      case DIG -> new DigMode();
    };
  }

  /**
   * Perform the titular search operation.
   */
  public final CompletableFuture<Result> search() {
    Journey.logger().debug(this + ": scheduling search");
    // kick off the first portion
    Journey.get().proxy().schedulingManager().schedule(this::asyncSearch, true);
    // Set up timeout task
    int timeout = flags().getValueFor(Flags.TIMEOUT);
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
  @Nullable
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

  @Synchronous
  public void initialize() {
    List<Mode> modeList = new LinkedList<>();
    for (ModeType modeType : agent.modeCapabilities()) {
      switch (modeType) {
        case FLY -> {
          if (!flags.getValueFor(Flags.FLY)) {
            continue;
          }
        }
        case DIG -> {
          if (!flags.getValueFor(Flags.DIG)) {
            continue;
          }
        }
      }
      if (modeType == ModeType.TUNNEL) {
        setTunnels(Journey.get().tunnelManager().tunnels(agent));
        continue;
      } else if (modeType == ModeType.FLY && !flags.getValueFor(Flags.FLY)) {
        continue;
      }

      Mode mode = buildMode(modeType);
      if (mode != null) {
        modeList.add(mode);
      }
    }
    setModes(modeList);
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

  public UUID getAgentUuid() {
    return agent.uuid();
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
    PLUGIN,
    OTHER
  }

  public record Result(ResultState state, @Nullable Itinerary itinerary) {
  }
}
