/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
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

package net.whimxiqal.journey.common.data.integration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.whimxiqal.journey.common.Journey;
import net.whimxiqal.journey.common.JourneyPlayer;
import net.whimxiqal.journey.common.command.JourneyExecutor;
import net.whimxiqal.journey.common.message.Formatter;
import net.whimxiqal.journey.common.navigation.Cell;
import net.whimxiqal.mantle.common.CommandSource;

public class Scope {

  private final String id;
  private final Component name;
  private final Component description;
  private final Map<String, Scope> subScopes;
  private final Supplier<Map<String, Cell>> items;

  public static Scope root(CommandSource src) {
    JourneyPlayer player = Journey.get().proxy().platform().onlinePlayer(src.uuid())
        .orElseThrow(RuntimeException::new); // programmer error

    ScopeBuilder builder = new ScopeBuilder(null);
    builder.addSubScope(Scope.builder(JourneyExecutor.PERSONAL_WAYPOINT_SCOPE)
            .name(Component.text("My Waypoints"))
            .description(Formatter.dull("Your personal waypoints"))
            .addItems(Journey.get().dataManager().personalWaypointManager().getAll(src.uuid(), false))
            .build())
        .addSubScope(Scope.builder(JourneyExecutor.PUBLIC_WAYPOINT_SCOPE)
            .name(Component.text("Server Waypoints"))
            .description(Formatter.dull("Public server waypoints"))
            .addItems(Journey.get().dataManager().publicWaypointManager().getAll())
            .build());
    Collection<JourneyPlayer> players = Journey.get().proxy().platform().onlinePlayers();
    if (players.size() > 1) {
      builder.addSubScope(Scope.builder("player")
          .name(Component.text("Players"))
          .description(Formatter.dull("Online players and their personal waypoints that they made public"))
          .addSubScopes(players.stream()
              .filter(p -> !p.uuid().equals(src.uuid()))
              .map(p -> Scope.builder(p.name())
                  .name(Component.text(p.name()))
                  .addItem("player", p.location())
                  .addSubScope(Scope.builder("personal")
                      .name(Component.text(p.name() + "'s Public Waypoints"))
                      .description(Formatter.dull("Go to this player"))
                      .addItems(() -> Journey.get().dataManager().personalWaypointManager().getAll(p.uuid(), true))
                      .build())
                  .build())
              .collect(Collectors.toList()))
          .build());
    }
    Journey.get().deathManager().getDeathLocation(src.uuid()).ifPresent(location -> builder.addItem("death", location));
    if (!Journey.get().proxy().platform().isAtSurface(player.location())) {
      // We can set null here because this item should *always* be handled specially!
      builder.addItem("surface", null);
    }

    for (Integrator integrator : Journey.get().integrationManager().integrators()) {
      builder.addSubScope(integrator.scope(src));
    }
    return builder.build();
  }

  public static ScopeBuilder builder(String name) {
    return new ScopeBuilder(name);
  }

  public Scope(String id, Component name, Component description,
               Map<String, Scope> subScopes,
               Supplier<Map<String, Cell>> items) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.subScopes = subScopes;
    this.items = items;
  }

  public String id() {
    return id;
  }

  public Component name() {
    return name;
  }

  public Component description() {
    return description;
  }

  public Map<String, Scope> subScopes() {
    return Collections.unmodifiableMap(subScopes);
  }

  public Map<String, Cell> items() {
    return Collections.unmodifiableMap(items.get());
  }

  public Collection<String> options() {
    if (id != null) {
      throw new RuntimeException(); // programmer error: must only be called on root
    }
    return options("", new HashSet<>());
  }

  private Collection<String> options(String currentScope, Set<String> allItems) {
    List<String> options = new LinkedList<>();
    for (String candidate : items.get().keySet()) {
      if (!allItems.contains(candidate)) {
        allItems.add(candidate);
        options.add(candidate);
      }
      options.add(currentScope + candidate);
    }
    for (Scope scope : subScopes.values()) {
      options.addAll(scope.options(currentScope + scope.id + ":", allItems));
    }
    return options;
  }

  public ScopedLocationResult location(String scopedString) {
    if (id != null) {
      throw new RuntimeException(); // programmer error
    }
    return location("", scopedString);
  }

  private ScopedLocationResult location(String scopeHistory, String scopedString) {
    String[] tokens = scopedString.split(":", 2);
    if (tokens.length == 1) {
      String item = tokens[0];  // = scopedString
      Map<String, Cell> retrievedItems = items.get();
      if (retrievedItems.containsKey(item)) {
        return ScopedLocationResult.exists(retrievedItems.get(item), scopeHistory);
      }
      // Not found in these items, maybe it's an item among our subscopes?
      // i.e. blah:foo:bar means bar can still show up on scope "blah"
      return locationFromSubScopes(scopeHistory, item);
    } else {
      Scope subScope = subScopes.get(tokens[0]);
      if (subScope == null) {
        return ScopedLocationResult.noScope(tokens[0]);
      }
      return subScope.location(scopeHistory + subScope.id + ":", tokens[1]);
    }
  }

  private ScopedLocationResult locationFromSubScopes(String scopeHistory, String scopedString) {
// Item not found in this scope. Perhaps it's just not scoped and is present in subscope?
    Cell destination = null;
    String existingScope = null;  // scope that our item was found under
    for (Scope subScope : subScopes.values()) {
      ScopedLocationResult result = subScope.location(scopeHistory + subScope.id + ":", scopedString);
      if (result.type() == ScopedLocationResult.Type.AMBIGUOUS) {
        // This has already been determined to be ambiguous. Propagate.
        return result;
      }
      if (result.type() == ScopedLocationResult.Type.EXISTS) {
        if (destination != null) {
          // We've now found two cells! Ambiguous!
          return ScopedLocationResult.ambiguous(existingScope, result.scope().get());
        }
        destination = result.location().get();
        existingScope = result.scope().get();
      }
    }
    if (destination == null) {
      // none found
      return ScopedLocationResult.none();
    }
    return ScopedLocationResult.exists(destination, existingScope);
  }

}
