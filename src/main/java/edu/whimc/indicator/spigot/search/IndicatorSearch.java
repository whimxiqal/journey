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

import com.google.common.collect.Lists;
import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.path.Link;
import edu.whimc.indicator.common.path.Mode;
import edu.whimc.indicator.common.search.TwoLevelBreadthFirstSearch;
import edu.whimc.indicator.spigot.cache.DebugManager;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.path.PortalLink;
import edu.whimc.indicator.spigot.path.mode.*;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.portals.Main;
import edu.whimc.portals.Portal;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class IndicatorSearch extends TwoLevelBreadthFirstSearch<LocationCell, World> {

  public static final List<Mode<LocationCell, World>> SURVIVAL_MODES = Lists.newArrayList(
      new WalkMode(),
      new JumpMode(),
      new SwimMode(),
      new DoorMode(),
      new ClimbMode());

  /**
   * Player constructor. Modes and links are registered according to the player's state and status
   *
   * @param player the in game player to create around
   */
  public IndicatorSearch(Player player, boolean nofly) {
    super(Indicator.getInstance().getTrailCache());
    // Modes - in order of preference
    if (player.getAllowFlight() && !nofly) {
      registerMode(new FlyMode());
    } else {
      registerMode(new WalkMode());
      registerMode(new JumpMode());
      registerMode(new SwimMode());
    }
    registerMode(new DoorMode());
    registerMode(new ClimbMode());

    // Links
    registerLinks(player::hasPermission, player);

    // Callbacks
    setCallbacks();
  }

  /**
   * Direct initializing constructor. Links are only registered if the permission predicate
   * matches with the behavior associated with the links.
   *
   * @param modes               the modes to use during searching
   * @param permissionPredicate a predicate to determine which links to add
   */
  public IndicatorSearch(Collection<Mode<LocationCell, World>> modes, Predicate<String> permissionPredicate) {
    super(Indicator.getInstance().getTrailCache());
    modes.forEach(this::registerMode);
    registerLinks(permissionPredicate);

    // Callbacks
    setCallbacks();
  }

  private void registerLinks(Predicate<String> permissionSupplier) {
    this.registerLinks(permissionSupplier, null);
  }

  private void registerLinks(Predicate<String> permissionSupplier, @Nullable Player player) {
    // Links - Nether
    Indicator.getInstance().getNetherManager().makeLinks().forEach(this::registerLink);

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
              return new PortalLink(portal);
            } catch (Exception e) {
              return null;
            }
          })
          .filter(Objects::nonNull)
          .forEach(link -> {
            if (player == null) {
              registerLink(link);
            } else {
              registerLinkVerbose(player, link);
            }
          });
    }
  }

  private void registerLinkVerbose(Player player, Link<LocationCell, World> link) {
    if (Indicator.getInstance().getDebugManager().isDebugging(player.getUniqueId())) {
      player.spigot().sendMessage(Format.debug("Registering Link: " + link.toString()));
    }
    super.registerLink(link);
  }

  private void setCallbacks() {
    DebugManager debugManager = Indicator.getInstance().getDebugManager();
    setStartTrailSearchCallback((origin, destination) -> {
      debugManager.broadcastDebugMessage(Format.PREFIX + Format.WARN + "Began" + Format.DEBUG + " a trail search: ");
      debugManager.broadcastDebugMessage(Format.debug(
          origin.toString()
              + " -> "));
      debugManager.broadcastDebugMessage(Format.debug(destination.toString()));
    });
    setFinishTrailSearchCallback((origin, destination, length) -> {
      debugManager.broadcastDebugMessage(Format.PREFIX + Format.SUCCESS + "Finished" + Format.DEBUG + " a trail search: ");
      debugManager.broadcastDebugMessage(Format.debug(
          origin.toString()
              + " -> "));
      debugManager.broadcastDebugMessage(Format.debug(destination.toString()));
      debugManager.broadcastDebugMessage(Format.debug("Length: " + (length > 1000000 ? "Inf" : Math.round(length))));
    });
    setMemoryCapacityErrorCallback((origin, destination) -> {
      debugManager.broadcastDebugMessage(Format.debug("Ran out of allocated memory for a local trail search: "));
      debugManager.broadcastDebugMessage(Format.debug(
          origin.toString()
              + " -> "));
      debugManager.broadcastDebugMessage(Format.debug(destination.toString()));
    });
  }

}
