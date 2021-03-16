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
import edu.whimc.indicator.api.search.TwoLevelBreadthFirstSearch;
import edu.whimc.indicator.spigot.cache.DebugManager;
import edu.whimc.indicator.spigot.path.LocationCell;
import edu.whimc.indicator.spigot.path.PortalLink;
import edu.whimc.indicator.spigot.search.mode.FlyMode;
import edu.whimc.indicator.spigot.search.mode.JumpMode;
import edu.whimc.indicator.spigot.search.mode.WalkMode;
import edu.whimc.indicator.spigot.util.Format;
import edu.whimc.portals.Main;
import edu.whimc.portals.Portal;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class IndicatorSearch extends TwoLevelBreadthFirstSearch<LocationCell, World> {

  public IndicatorSearch(Player player) {
    // Modes
    registerMode(new WalkMode());
    registerMode(new JumpMode());
    if (player.getGameMode().equals(GameMode.CREATIVE)) {
      registerMode(new FlyMode());
    }

    // Links
    Indicator.getInstance().getNetherManager().makeLinks().forEach(this::registerLink);
    // Links - Portals
    Plugin plugin = Bukkit.getPluginManager().getPlugin("WHIMC-Portals");
    if (plugin instanceof Main) {
      Portal.getPortals().stream().map(PortalLink::new).forEach(this::registerLink);
    }

    // Callbacks
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
      debugManager.broadcastDebugMessage(Format.debug("Began a trail search: "));
      debugManager.broadcastDebugMessage(Format.debug(
          origin.toString()
              + " -> "));
      debugManager.broadcastDebugMessage(Format.debug(destination.toString()));
    });
  }

}
