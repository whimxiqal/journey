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

package edu.whimc.journey.spigot.search;

import edu.whimc.journey.common.navigation.Port;
import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.navigation.WhimcPortalPort;
import edu.whimc.journey.spigot.navigation.mode.ClimbMode;
import edu.whimc.journey.spigot.navigation.mode.DoorMode;
import edu.whimc.journey.spigot.navigation.mode.FlyMode;
import edu.whimc.journey.spigot.navigation.mode.JumpMode;
import edu.whimc.journey.spigot.navigation.mode.SwimMode;
import edu.whimc.journey.spigot.navigation.mode.WalkMode;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.portals.Main;
import edu.whimc.portals.Portal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * A search session designed to be used for all players.
 */
public class PlayerSearchSession extends SpigotSearchSession {

  @Getter
  private final PlayerSessionState sessionInfo;
  @Getter
  private final AnimationManager animationManager;

  /**
   * General constructor.
   *
   * <p>The algorithm step delay is primarily used for animation.
   *
   * @param player             the player
   * @param flags              any search flags, each of which may alter the search algorithm
   * @param algorithmStepDelay how long to wait between every search step
   */
  public PlayerSearchSession(Player player, Set<SearchFlag> flags, int algorithmStepDelay) {
    super(player.getUniqueId(), Caller.PLAYER);
    this.sessionInfo = new PlayerSessionState();
    this.animationManager = new AnimationManager(this);
    animationManager.setAnimating(flags.contains(SearchFlag.ANIMATE));
    setAlgorithmStepDelay(algorithmStepDelay);

    // Modes
    registerModes(player, flags);

    // Ports
    registerNetherPorts();
    registerWhimcPortalPorts(player::hasPermission);
  }


}
