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

import edu.whimc.indicator.Indicator;
import edu.whimc.indicator.common.cache.TrailCache;
import edu.whimc.indicator.common.path.Mode;
import edu.whimc.indicator.common.search.TwoLevelBreadthFirstSearch;
import edu.whimc.indicator.spigot.cache.DebugManager;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.path.PortalLink;
import edu.whimc.indicator.spigot.path.mode.FlyMode;
import edu.whimc.indicator.spigot.path.mode.JumpMode;
import edu.whimc.indicator.spigot.path.mode.SwimMode;
import edu.whimc.indicator.spigot.path.mode.WalkMode;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.portals.Main;
import edu.whimc.portals.Portal;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class IndicatorSearch extends TwoLevelBreadthFirstSearch<LocationCell, World> {

  public IndicatorSearch(Player player) {
    super(Indicator.getInstance().getTrailCache());
    // Modes
    registerMode(new WalkMode());
    registerMode(new JumpMode());
    registerMode(new SwimMode());
    if (player.getGameMode().equals(GameMode.CREATIVE)) {
      registerMode(new FlyMode());
    }

    // Links
    registerLinks(player::hasPermission);

    // Callbacks
    setCallbacks();
  }

  public IndicatorSearch(List<Mode<LocationCell, World>> modes, Predicate<String> permissionPredicate) {
    super(Indicator.getInstance().getTrailCache());
    modes.forEach(this::registerMode);
    registerLinks(permissionPredicate);

    // Callbacks
    setCallbacks();
  }

  private void registerLinks(Predicate<String> permissionSupplier) {
    // Links - Nether
    Indicator.getInstance().getNetherManager().makeLinks().forEach(this::registerLink);

    // Links - Portals plugin
    Plugin plugin = Bukkit.getPluginManager().getPlugin("WHIMC-Portals");
    if (plugin instanceof Main) {
      Portal.getPortals().stream()
          .filter(portal ->
              Optional.ofNullable(portal.getPermission()).map(perm ->
                  permissionSupplier.test(perm.getName())).orElse(true))
          .map(PortalLink::new)
          .forEach(this::registerLink);
    }
  }

  private void setCallbacks() {
    DebugManager debugManager = Indicator.getInstance().getDebugManager();
    setStartTrailSearchCallback((origin, destination) -> {
      debugManager.broadcastDebugMessage(Format.debug("Began a trail search: "));
      debugManager.broadcastDebugMessage(Format.debug(
          origin.toString()
              + " -> "));
      debugManager.broadcastDebugMessage(Format.debug(destination.toString()));
    });
    setFinishTrailSearchCallback((origin, destination) -> {
      debugManager.broadcastDebugMessage(Format.debug("Finished a trail search: "));
      debugManager.broadcastDebugMessage(Format.debug(
          origin.toString()
              + " -> "));
      debugManager.broadcastDebugMessage(Format.debug(destination.toString()));
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
