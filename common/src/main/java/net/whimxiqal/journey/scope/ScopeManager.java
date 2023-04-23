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

package net.whimxiqal.journey.scope;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.Destination;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.JourneyPlayer;
import net.whimxiqal.journey.Scope;
import net.whimxiqal.journey.VirtualMap;
import net.whimxiqal.journey.message.Formatter;
import net.whimxiqal.journey.search.InternalScope;
import net.whimxiqal.journey.search.PlayerDomainGoalSearchSession;
import net.whimxiqal.journey.search.SearchSession;
import net.whimxiqal.journey.util.Permission;
import net.whimxiqal.journey.util.Validator;

public class ScopeManager {

  private final Map<String, InternalScope> scopes = new HashMap<>();
  private final Map<String, String> plugins = new HashMap<>();

  public void registerDefault() {
    register(Journey.NAME, "personal", Scope.builder()
        .name(Component.text("My Waypoints"))
        .destinations(player -> VirtualMap.of(
            () -> Journey.get().dataManager().personalWaypointManager()
                .getAll(player.uuid(), false)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Destination.of(entry.getValue()))),
            Journey.get().dataManager().personalWaypointManager().getCount(player.uuid(), false)))
        .permission(Permission.PATH_PERSONAL.path())
        .build());
    register(Journey.NAME, "server", Scope.builder()
        .name(Component.text("Server Waypoints"))
        .destinations(player -> VirtualMap.of(
            () -> Journey.get().dataManager().publicWaypointManager().getAll()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Destination.of(entry.getValue()))),
            Journey.get().dataManager().publicWaypointManager().getCount()))
        .permission(Permission.PATH_SERVER.path())
        .build());
    register(Journey.NAME, "player", Scope.builder()
        .name(Component.text("Players"))
        .description(Formatter.dull("Online players and their"),
            Formatter.dull("public personal waypoints"))
        .subScopes(player -> VirtualMap.of(Journey.get().proxy().platform()
            .onlinePlayers()
            .stream()
            .filter(p -> !p.uuid().equals(player.uuid()))
            .collect(Collectors.<JourneyPlayer, String, Scope>toMap(JourneyPlayer::name, p -> Scope.builder()
                .name(Component.text(p.name()))
                .destinations(() -> VirtualMap.ofSingleton(p.name(), Destination.builder(p.location()).permission(Permission.PATH_PLAYER_ENTITY.path()).build()))
                .subScopes(() -> VirtualMap.ofSingleton("waypoints", Scope.builder()
                    .name(Component.text(p.name() + "'s Waypoints"))
                    .description(Formatter.dull("Go to this player"))
                    .permission(Permission.PATH_PLAYER_WAYPOINTS.path())
                    .destinations(VirtualMap.of(
                        () -> Journey.get().dataManager().personalWaypointManager().getAll(p.uuid(), true)
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> Destination.of(entry.getValue()))),
                        Journey.get().dataManager().personalWaypointManager().getCount(p.uuid(), true)))
                    .build()))
                .strict()  // to access any player destinations, you must at least scope to the player
                .build()))))
        .build());
    register(Journey.NAME, "world", new InternalScope(Scope.builder()
            .name(Component.text("Worlds"))
        .build(),
        p1 -> VirtualMap.empty(),
        p1 -> VirtualMap.of(Journey.get().proxy().platform().domainResourceKeys()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                new InternalScope(Scope.builder()
                    .name(Component.text(entry.getKey()))
                    .build(),
                    p2 -> VirtualMap.of(entry.getValue().entrySet().stream()
                        .filter(entry2 -> entry2.getValue() != p1.location().domain())  // can't request to go to their current domain
                        .collect(Collectors.toMap(Map.Entry::getKey, entry2 -> {
                          SearchSession session = new PlayerDomainGoalSearchSession(p2, entry2.getValue());
                          session.addPermission(Permission.PATH_WORLD.path());
                          return session;
                        }))),
                    p2 -> VirtualMap.empty()))))));
  }

  public void register(String plugin, String id, Scope scope) {
    register(plugin, id, new InternalScope(scope));
  }

  private void register(String plugin, String id, InternalScope scope) {
    if (Validator.isInvalidDataName(id)) {
      throw new IllegalArgumentException("Scope id '" + id + "' is invalid");
    }
    if (scopes.containsKey(id)) {
      throw new IllegalArgumentException("A scope with id " + id + " already exists");
    }
    scopes.put(id, scope);
    plugins.put(id, plugin);
  }

  public Map<String, InternalScope> scopes() {
    return scopes;
  }

  public String plugin(String scopeId) {
    return plugins.get(scopeId);
  }

  public void initialize() {
    registerDefault();
  }
}
