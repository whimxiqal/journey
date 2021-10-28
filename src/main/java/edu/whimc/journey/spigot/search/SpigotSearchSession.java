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
 *
 */

package edu.whimc.journey.spigot.search;

import edu.whimc.journey.common.search.ReverseSearchSession;
import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.navigation.WhimcPortalPort;
import edu.whimc.journey.spigot.navigation.mode.ClimbMode;
import edu.whimc.journey.spigot.navigation.mode.DoorMode;
import edu.whimc.journey.spigot.navigation.mode.FlyMode;
import edu.whimc.journey.spigot.navigation.mode.JumpMode;
import edu.whimc.journey.spigot.navigation.mode.SwimMode;
import edu.whimc.journey.spigot.navigation.mode.WalkMode;
import edu.whimc.portals.Main;
import edu.whimc.portals.Portal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * A search session implemented specifically for Spigot.
 */
public abstract class SpigotSearchSession extends ReverseSearchSession<LocationCell, World> {
  protected SpigotSearchSession(UUID callerId, Caller callerType) {
    super(callerId, callerType);
  }

  /**
   * Same as {@link #search(LocationCell, LocationCell)}.
   *
   * @param origin      the location at which to start the search
   * @param destination the location at which to end the search
   */
  public final void search(Location origin, Location destination) {
    this.search(new LocationCell(origin), new LocationCell(destination));
  }

  /**
   * Register modes for a player.
   *
   * @param player the player
   */
  protected final void registerModes(Player player, Set<SearchFlag> flags) {
    Set<Material> passableBlocks = collectPassableBlocks(flags);

    // Register modes in order of preference
    if (player.getAllowFlight() && !flags.contains(SearchFlag.NOFLY)) {
      registerMode(new FlyMode(this, passableBlocks));
    } else {
      registerMode(new WalkMode(this, passableBlocks));
      registerMode(new JumpMode(this, passableBlocks));
      registerMode(new SwimMode(this, passableBlocks));
    }
    registerMode(new DoorMode(this, passableBlocks));
    registerMode(new ClimbMode(this, passableBlocks));
  }

  /**
   * Register all {@link edu.whimc.journey.spigot.navigation.NetherPort}s.
   */
  protected final void registerNetherPorts() {
    JourneySpigot.getInstance().getNetherManager().makePorts().forEach(this::registerPort);
  }

  /**
   * Register all {@link WhimcPortalPort}s.
   *
   * @param permissionAccess a predicate that determines whether there is permission
   *                         for the given permissions string
   */
  protected final void registerWhimcPortalPorts(Predicate<String> permissionAccess) {
    Plugin plugin = Bukkit.getPluginManager().getPlugin("WHIMC-Portals");
    if (plugin instanceof Main) {
      Portal.getPortals().stream()
          .filter(portal -> portal.getDestination() != null)
          .filter(portal -> portal.getWorld() != null)
          .filter(portal -> portal.getDestination().getLocation().getWorld() != null)
          .filter(portal -> Optional.ofNullable(portal.getPermission()).map(perm ->
              permissionAccess.test(perm.getName())).orElse(true))
          .map(portal -> {
            try {
              return WhimcPortalPort.from(portal);
            } catch (Exception e) {
              return null;
            }
          })
          .filter(Objects::nonNull)
          .forEach(this::registerPort);
    }
  }

  protected final Set<Material> collectPassableBlocks(Set<SearchFlag> flags) {
    Set<Material> passableBlocks = new HashSet<>();
    if (flags.contains(SearchFlag.NODOOR)) {
      passableBlocks.add(Material.ACACIA_DOOR);
      passableBlocks.add(Material.ACACIA_TRAPDOOR);
      passableBlocks.add(Material.BIRCH_DOOR);
      passableBlocks.add(Material.BIRCH_TRAPDOOR);
      passableBlocks.add(Material.CRIMSON_DOOR);
      passableBlocks.add(Material.CRIMSON_TRAPDOOR);
      passableBlocks.add(Material.DARK_OAK_DOOR);
      passableBlocks.add(Material.DARK_OAK_TRAPDOOR);
      passableBlocks.add(Material.IRON_DOOR);
      passableBlocks.add(Material.JUNGLE_DOOR);
      passableBlocks.add(Material.JUNGLE_TRAPDOOR);
      passableBlocks.add(Material.OAK_DOOR);
      passableBlocks.add(Material.OAK_TRAPDOOR);
      passableBlocks.add(Material.SPRUCE_DOOR);
      passableBlocks.add(Material.SPRUCE_TRAPDOOR);
      passableBlocks.add(Material.WARPED_DOOR);
      passableBlocks.add(Material.WARPED_TRAPDOOR);
    }
    return passableBlocks;
  }

}
