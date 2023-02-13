package net.whimxiqal.journey.scope;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import net.whimxiqal.journey.Destination;
import net.whimxiqal.journey.JourneyPlayer;
import net.whimxiqal.journey.VirtualMap;
import net.whimxiqal.journey.Permissible;
import net.whimxiqal.journey.Scope;
import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.util.Permission;
import net.whimxiqal.journey.util.Validator;

public final class ScopeUtil {

  public final static long MAX_SIZE_TO_SHOW_OPTIONS = 128;

  private ScopeUtil() {
  }

  public static Scope root() {
    return Scope.builder()
        .destinations(player -> {
          Map<String, Destination> destinations = new HashMap<>();
          Journey.get().deathManager().getDeathLocation(player.uuid()).ifPresent(location ->
              destinations.put("death", Destination.builder(location).permission(Permission.PATH_DEATH.path()).build()));

          if (!Journey.get().proxy().platform().isAtSurface(player.location())) {
            // We can set null here because this item should *always* be handled specially!
            destinations.put("surface", Destination.builder(null).permission(Permission.PATH_SURFACE.path()).build());
          }
          return VirtualMap.of(destinations);
        })
        .subScopes(VirtualMap.of(Journey.get().scopeManager().scopes()))
        .build();
  }

  public static Collection<String> options(JourneyPlayer player) {
    return options(null, root(), player, new Stack<>(), new HashSet<>(), Integer.MAX_VALUE);
  }

  private static Collection<String> options(String plugin, Scope scope, JourneyPlayer player, Stack<String> scopeHistory, Set<String> options, int minScopeIndexRequired) {
    VirtualMap<Scope> subScopes = scope.subScopes(player);
    VirtualMap<Destination> destinations = scope.destinations(player);
    if (scope.isStrict()) {
      minScopeIndexRequired = Math.min(scopeHistory.size() - 1, minScopeIndexRequired);
    }
    int totalSize = subScopes.size() + destinations.size();
    if (totalSize > MAX_SIZE_TO_SHOW_OPTIONS) {
      return options;
    }
    for (Map.Entry<String, ? extends Destination> entry : destinations.getAll().entrySet()) {
      if (Validator.isInvalidDataName(entry.getKey())) {
        if (plugin != null) {
          Journey.logger().warn("Destination from plugin "
              + plugin + " is using an invalid destination id: "
              + entry.getKey() + ". Please notify the plugin owner.");
        }
        continue;
      }
      if (restricted(entry.getValue(), player)) {
        continue;
      }
      // add with scoping (from most specific to least specific)
      LinkedList<String> scopeStrings = new LinkedList<>();
      StringBuilder currentScope = new StringBuilder();
      for (int i = scopeHistory.size() - 1; i >= 0; i--) {
        // is the destination name the same as the scope directly above it?
        if (i == scopeHistory.size() - 1 && scopeHistory.peek().equals(entry.getKey())) {
          // we just use the name once if a destination uses the same name as its scope
          continue;
        }
        // the scope string must include the scope at the index minScopeIndexRequired (which would have been added last iteration)
        if (i < minScopeIndexRequired) {
          scopeStrings.add(currentScope.toString());
        }
        currentScope.insert(0, scopeHistory.get(i) + ":");
      }
      // the entire scope history has been concatenated -- this will always be included
      scopeStrings.add(currentScope.toString());
      for (String scopeString : scopeStrings) {
        String newCandidate = scopeString + entry.getKey();
        if (options.contains(newCandidate)) {
          options.remove(newCandidate);  // if left in there, it would be ambiguous
        } else {
          options.add(newCandidate);
        }
      }
    }
    for (Map.Entry<String, ? extends Scope> subScope : subScopes.getAll().entrySet()) {
      String pluginName = plugin == null ? Journey.get().scopeManager().plugin(subScope.getKey()) : plugin;
      if (Validator.isInvalidDataName(subScope.getKey())) {
        Journey.logger().warn("Scope from plugin "
            + pluginName + " is using an invalid sub-scope id: "
            + subScope.getKey() + ". Please notify the plugin owner.");
        continue;
      }
      if (restricted(subScope.getValue(), player)) {
        continue;
      }
      scopeHistory.push(subScope.getKey());
      options.addAll(options(pluginName, subScope.getValue(), player, scopeHistory, options, minScopeIndexRequired));
      scopeHistory.pop();
    }
    return options;
  }

  public static ScopedLocationResult location(JourneyPlayer player, String scopedString) {
    return location(root(), player, "", scopedString);
  }

  private static ScopedLocationResult location(Scope scope, JourneyPlayer player, String scopeHistory, String scopedString) {
    String[] tokens = scopedString.split(":", 2);
    String item = tokens[0];  // = scopedString
    if (tokens.length == 1) {
      // this is the last item, so it must either be a direct call to a destination or an indirect call to a destination
      // that's part of some sub-scope.
      Destination dest = scope.destinations(player).get(item);
      if (dest != null) {
        // it's the name of a destination
        if (restricted(dest, player)) {
          return ScopedLocationResult.noPermission();
        } else {
          return ScopedLocationResult.exists(dest, scopeHistory);
        }
      }
      // maybe this is the name of a sub-scope, indicating that a destination of the sub-scope with the same name is the goal
      VirtualMap<Scope> subScopes = scope.subScopes(player);
      Scope subScope = subScopes.get(item);
      if (subScope != null) {
        dest = subScope.destinations(player).get(item);
        if (dest != null) {
          // it's the name of a destination hidden under the name of the identically named sub-scope.
          if (restricted(dest, player)) {
            return ScopedLocationResult.noPermission();
          } else {
            return ScopedLocationResult.exists(dest, scopeHistory);
          }
        }
      }
      // Not found in these items, maybe it's an item among our sub-scopes?
      // i.e. blah:foo:bar means "bar" can still show up on scope "blah"
      return locationFromSubScopes(subScopes.getAll(), player, scopeHistory, scopedString);
    } else {
      VirtualMap<Scope> subScopes = scope.subScopes(player);
      Scope subScope = subScopes.get(item);
      if (subScope == null) {
        return locationFromSubScopes(subScopes.getAll(), player, scopeHistory, scopedString);
      }
      if (restricted(subScope, player)) {
        return ScopedLocationResult.noPermission();
      }
      return location(subScope, player, scopeHistory + item + ":", tokens[1]);
    }
  }

  private static ScopedLocationResult locationFromSubScopes(Map<String, ? extends Scope> subScopes, JourneyPlayer player, String scopeHistory, String scopedString) {
    // Item not found in this scope. Perhaps it's just not scoped and is present in a subscope?
    Destination destination = null;
    String existingScope = null;  // scope that our item was found under
    for (Map.Entry<String, ? extends Scope> subScope : subScopes.entrySet()) {
      if (subScope.getValue().isStrict()) {
        continue;
      }
      if (restricted(subScope.getValue(), player)) {
        continue;
      }
      ScopedLocationResult result = location(subScope.getValue(), player, scopeHistory + subScope.getKey() + ":", scopedString);
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
    if (restricted(destination, player)) {
      return ScopedLocationResult.noPermission();
    }
    return ScopedLocationResult.exists(destination, existingScope);
  }

  public static boolean restricted(Permissible permissible, JourneyPlayer player) {
    Optional<String> permission = permissible.permission();
    return permission.isPresent() && !player.hasPermission(permission.get());
  }

}
