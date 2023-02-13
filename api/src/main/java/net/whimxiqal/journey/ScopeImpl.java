package net.whimxiqal.journey;

import java.util.Optional;
import java.util.function.Function;
import net.kyori.adventure.text.Component;

class ScopeImpl implements Scope {

  private final Component name;
  private final Component description;
  private final Function<JourneyPlayer, VirtualMap<Scope>> subScopes;
  private final Function<JourneyPlayer, VirtualMap<Destination>> destinations;
  private final String permission;
  private final boolean strict;

  ScopeImpl(Component name, Component description,
            Function<JourneyPlayer, VirtualMap<Scope>> subScopes,
            Function<JourneyPlayer, VirtualMap<Destination>> destinations,
            String permission,
            boolean strict) {
    this.name = name;
    this.description = description;
    this.subScopes = subScopes;
    this.destinations = destinations;
    this.permission = permission;
    this.strict = strict;
  }

  @Override
  public Component name() {
    return name;
  }

  @Override
  public Component description() {
    return description;
  }

  @Override
  public VirtualMap<Scope> subScopes(JourneyPlayer player) {
    if (subScopes == null) {
      return Scope.super.subScopes(player);
    }
    return subScopes.apply(player);
  }

  @Override
  public VirtualMap<Destination> destinations(JourneyPlayer player) {
    if (destinations == null) {
      return Scope.super.destinations(player);
    }
    return destinations.apply(player);
  }

  @Override
  public Optional<String> permission() {
    return Optional.ofNullable(permission);
  }

  @Override
  public boolean isStrict() {
    return strict;
  }

}
