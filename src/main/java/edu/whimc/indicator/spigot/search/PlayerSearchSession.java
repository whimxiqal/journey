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

package edu.whimc.indicator.spigot.search;

import edu.whimc.indicator.common.search.ResultState;
import edu.whimc.indicator.spigot.IndicatorSpigot;
import edu.whimc.indicator.common.navigation.Leap;
import edu.whimc.indicator.common.search.ReverseSearchSession;
import edu.whimc.indicator.spigot.navigation.LocationCell;
import edu.whimc.indicator.spigot.navigation.PortalLeap;
import edu.whimc.indicator.spigot.navigation.mode.ClimbMode;
import edu.whimc.indicator.spigot.navigation.mode.DoorMode;
import edu.whimc.indicator.spigot.navigation.mode.FlyMode;
import edu.whimc.indicator.spigot.navigation.mode.JumpMode;
import edu.whimc.indicator.spigot.navigation.mode.SwimMode;
import edu.whimc.indicator.spigot.navigation.mode.WalkMode;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.portals.Main;
import edu.whimc.portals.Portal;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class PlayerSearchSession extends ReverseSearchSession<LocationCell, World> {

  @Getter
  private final SessionState sessionInfo;
  @Getter
  private final AnimationManager animationManager;

  public PlayerSearchSession(Player player, Set<SearchFlag> flags, int algorithmStepDelay) {
    super(player.getUniqueId(), Caller.PLAYER);
    this.sessionInfo = new SessionState();
    this.animationManager = new AnimationManager(this);
    animationManager.setAnimating(flags.contains(SearchFlag.ANIMATE));
    setAlgorithmStepDelay(algorithmStepDelay);
    // Modes - in order of preference
    if (player.getAllowFlight() && !flags.contains(SearchFlag.NOFLY)) {
      registerMode(new FlyMode());
    } else {
      registerMode(new WalkMode());
      registerMode(new JumpMode());
      registerMode(new SwimMode());
    }
    registerMode(new DoorMode());
    registerMode(new ClimbMode());

    // Links
    registerLeaps(player::hasPermission, player);
  }

  private void registerLeaps(Predicate<String> permissionSupplier) {
    this.registerLeaps(permissionSupplier, null);
  }

  private void registerLeaps(Predicate<String> permissionSupplier, @Nullable Player player) {
    // Links - Nether
    IndicatorSpigot.getInstance().getNetherManager().makeLeaps().forEach(this::registerLeap);

    // Links - Portals plugin
    Plugin plugin = Bukkit.getPluginManager().getPlugin("WHIMC-Portals");
    if (plugin instanceof Main) {
      Portal.getPortals().stream()
          .filter(portal -> portal.getDestination() != null)
          .filter(portal -> portal.getWorld() != null)
          .filter(portal -> portal.getDestination().getLocation().getWorld() != null)
          .filter(portal -> Optional.ofNullable(portal.getPermission()).map(perm ->
              permissionSupplier.test(perm.getName())).orElse(true))
          .map(portal -> {
            try {
              return PortalLeap.from(portal);
            } catch (Exception e) {
              return null;
            }
          })
          .filter(Objects::nonNull)
          .forEach(link -> {
            if (player == null) {
              registerLeap(link);
            } else {
              registerLinkVerbose(player, link);
            }
          });
    }
  }

  private void registerLinkVerbose(Player player, Leap<LocationCell, World> link) {
    if (IndicatorSpigot.getInstance().getDebugManager().isDebugging(player.getUniqueId())) {
      player.spigot().sendMessage(Format.debug("Registering Link: " + link.toString()));
    }
    super.registerLeap(link);
  }

}
