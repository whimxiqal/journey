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

package net.whimxiqal.journey.integrations.multiverse;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseNetherPortals.MultiverseNetherPortals;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.Destination;
import net.whimxiqal.journey.JourneyPlayer;
import net.whimxiqal.journey.Scope;
import net.whimxiqal.journey.VirtualMap;
import net.whimxiqal.journey.bukkit.JourneyBukkitApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class MultiverseScope implements Scope {

  private MultiverseCore multiverseCore;
  private MultiversePortals multiversePortals;
  private MultiverseNetherPortals multiverseNetherPortals;

  MultiverseScope() {
    Plugin multiverseCore = Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
    if (multiverseCore == null) {
      JourneyMultiverse.logger().severe("Could not find Multiverse-Core");
      Bukkit.getPluginManager().disablePlugin(JourneyMultiverse.instance());
      return;
    }

    Plugin multiversePortals = Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Portals");
    if (multiversePortals instanceof MultiversePortals) {
      this.multiversePortals = (MultiversePortals) multiversePortals;
    }

    Plugin multiverseNetherPortals = Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Portals");
    if (multiversePortals instanceof MultiverseNetherPortals) {
      this.multiverseNetherPortals = (MultiverseNetherPortals) multiverseNetherPortals;
    }

  }

  @Override
  public Component name() {
    return Component.text("Multiverse");
  }

  @Override
  public VirtualMap<Scope> subScopes(JourneyPlayer player) {
    Map<String, Scope> map = new HashMap<>();

    // Anchors
    map.put("anchors", Scope.builder()
        .name(Component.text("Portals"))
        .destinations(VirtualMap.of(multiverseCore.getAnchorManager().getAnchors(Bukkit.getPlayer(player.uuid())).stream().collect(Collectors.toMap(
            anchor -> anchor,
            anchor -> Destination.cellBuilder(JourneyBukkitApi.get()
                    .toCell(multiverseCore.getAnchorManager().getAnchorLocation(anchor)))
                .name(Component.text(anchor))
                .build()
        ))))
        .build());

    // Portals
    map.put("portals", Scope.builder()
        .name(Component.text("Portals"))
        .destinations(VirtualMap.of(multiversePortals.getPortalManager().getAllPortals().stream().collect(Collectors.toMap(
            MVPortal::getName,
            portal -> Destination.boxBuilder(
                JourneyBukkitApi.get()
                    .toCell(portal.getLocation()
                        .getMinimum()
                        .toLocation(portal.getLocation()
                            .getMVWorld()
                            .getCBWorld())),
                    JourneyBukkitApi.get()
                        .toCell(portal.getLocation()
                            .getMinimum()
                            .toLocation(portal.getLocation()
                                .getMVWorld()
                                .getCBWorld())))
                .name(Component.text(portal.getName()))
                .build()
        ))))
        .build());

    return VirtualMap.of(map);
  }

  @Override
  public VirtualMap<Destination> destinations(JourneyPlayer player) {
    return Scope.super.destinations(player);
  }

  @Override
  public boolean isStrict() {
    return Scope.super.isStrict();
  }

  @Override
  public Optional<String> permission() {
    return Scope.super.permission();
  }
}
