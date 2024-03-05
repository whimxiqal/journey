/*
 * MIT License
 *
 * Copyright 2023 Pieter Svenson
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

package net.whimxiqal.journey.integration.essentialss;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.Cell;
import net.whimxiqal.journey.Destination;
import net.whimxiqal.journey.JourneyApiProvider;
import net.whimxiqal.journey.Scope;
import net.whimxiqal.journey.VirtualMap;
import net.whimxiqal.journey.sponge.JourneySpongeApiProvider;
import org.apache.logging.log4j.Logger;
import org.essentialss.api.EssentialsSAPI;
import org.essentialss.api.player.data.SGeneralUnloadedData;
import org.essentialss.api.world.SWorldData;
import org.essentialss.api.world.points.home.SHome;
import org.essentialss.api.world.points.warp.SWarp;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

/**
 * The main class of your Sponge plugin.
 *
 * <p>All methods are optional -- some common event registrations are included as a jumping-off point.</p>
 */
@Plugin("journey-essentialss")
public class JourneyEssentialsS {

  @Inject
  private PluginContainer container;

  @Inject
  private Logger logger;

  @Listener
  public void onStartedServer(final StartedEngineEvent<Server> event) {
    JourneyApiProvider.get().registerScope(container.metadata().id(),
        "essentials",
        Scope.builder()
            .name(Component.text("EssentialsS"))
            .subScopes(VirtualMap.of(Map.of(
                "homes", Scope.builder()
                    .name(Component.text("Homes"))
                    .destinations(player -> {
                      Optional<SGeneralUnloadedData> data = EssentialsSAPI.get()
                          .playerManager()
                          .get()
                          .dataFor(player.uuid());
                      return data.map(sGeneralUnloadedData -> VirtualMap.of(() -> {
                        Map<String, Destination> destinations = new HashMap<>();
                        for (SHome home : sGeneralUnloadedData.homes()) {
                          Optional<Cell> location = home.spongeLocation()
                              .flatMap(Location::onServer)
                              .map(serverLocation -> JourneySpongeApiProvider.get().toCell(serverLocation));
                          if (location.isEmpty()) {
                            // could not get the location
                            continue;
                          }
                          destinations.put(home.identifier(), Destination.builder(location.get())
                              .name(Component.text(home.identifier()))
                              .build());
                        }
                        return destinations;
                      }, sGeneralUnloadedData.homes().size())).orElseGet(VirtualMap::empty);
                    })
                    .build(),
                "warps", Scope.builder()
                    .name(Component.text("Warps"))
                    .subScopes(() -> {
                      Map<String, Map<String, Scope>> worldScopes = new HashMap<>();
                      for (SWorldData worldData : EssentialsSAPI.get().worldManager().get().allWorldData()) {
                        Optional<ResourceKey> key = worldData.worldKey();
                        if (key.isEmpty()) {
                          // we need the key
                          continue;
                        }
                        Map<String, Scope> namespaceScopes = worldScopes.computeIfAbsent(key.get().namespace(), k -> new HashMap<>());
                        namespaceScopes.put(key.get().value(), Scope.builder()
                            .name(Component.text(key.get().value()))
                            .destinations(player -> {
                              Map<String, Destination> warpDestinations = new HashMap<>();
                              for (SWarp warp : worldData.warps()) {
                                Optional<Cell> location = warp.spongeLocation()
                                    .flatMap(Location::onServer)
                                    .map(serverLocation -> JourneySpongeApiProvider.get().toCell(serverLocation));
                                if (location.isEmpty()) {
                                  // could not get the location
                                  continue;
                                }
                                warpDestinations.put(warp.identifier(), Destination.builder(location.get())
                                    .name(Component.text(warp.identifier()))
                                    .build());
                              }
                              return VirtualMap.of(warpDestinations);
                            })
                            .build());
                      }
                      return VirtualMap.of(worldScopes.entrySet()
                          .stream()
                          .collect(Collectors.toMap(Map.Entry::getKey,
                              entry -> Scope.builder()
                                  .name(Component.text(entry.getKey()))
                                  .subScopes(VirtualMap.of(entry.getValue()))
                                  .build())));
                    })
                    .build())))
            .build());
  }

}
