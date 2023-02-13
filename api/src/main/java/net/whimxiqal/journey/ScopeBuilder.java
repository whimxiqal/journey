package net.whimxiqal.journey;

import java.util.function.Function;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;

/**
 * A {@link Builder} for a {@link Scope}.
 */
public class ScopeBuilder implements Builder<Scope> {

  private Component name = Component.empty();
  private Component description = Component.empty();
  private Function<JourneyPlayer, VirtualMap<Scope>> subScopes;
  private Function<JourneyPlayer, VirtualMap<Destination>> destinations;
  private String permission = null;
  private boolean strict = false;

  ScopeBuilder() {
  }

  /**
   * Set the name.
   *
   * @param name the name
   * @return the builder, for chaining
   */
  public ScopeBuilder name(Component name) {
    this.name = name;
    return this;
  }

  /**
   * Set the description.
   *
   * @param description the description
   * @return the builder, for chaining
   */
  public ScopeBuilder description(Component description) {
    this.description = description;
    return this;
  }

  /**
   * Set the supplier of sub-scopes, where the sub-scopes are based on a player.
   *
   * @param subScopes the sub-scopes
   * @return the builder, for chaining
   */
  public ScopeBuilder subScopes(Function<JourneyPlayer, VirtualMap<Scope>> subScopes) {
    this.subScopes = subScopes;
    return this;
  }

  /**
   * Set the supplier of sub-scopes, where the sub-scopes are not based on a player.
   *
   * @param subScopes the sub-scopes
   * @return the builder, for chaining
   */
  public ScopeBuilder subScopes(Supplier<VirtualMap<Scope>> subScopes) {
    this.subScopes = unusedPlayer -> subScopes.get();
    return this;
  }

  /**
   * Set the supplier of sub-scopes, where the sub-scopes are already computed.
   *
   * @param subScopes the sub-scopes
   * @return the builder, for chaining
   */
  public ScopeBuilder subScopes(VirtualMap<Scope> subScopes) {
    this.subScopes = unusedPlayer -> subScopes;
    return this;
  }

  /**
   * Set the supplier of destinations, where the destinations are based on a player.
   *
   * @param destinations the destinations
   * @return the builder, for chaining
   */
  public ScopeBuilder destinations(Function<JourneyPlayer, VirtualMap<Destination>> destinations) {
    this.destinations = destinations;
    return this;
  }

  /**
   * Set the supplier of destinations, where the destinations are not based on a player.
   *
   * @param destinations the destinations
   * @return the builder, for chaining
   */
  public ScopeBuilder destinations(Supplier<VirtualMap<Destination>> destinations) {
    this.destinations = unusedPlayer -> destinations.get();
    return this;
  }

  /**
   * Set the supplier of destinations, where the destinations are already computed.
   *
   * @param destinations the destinations
   * @return the builder, for chaining
   */
  public ScopeBuilder destinations(VirtualMap<Destination> destinations) {
    this.destinations = unusedPlayer -> destinations;
    return this;
  }

  public ScopeBuilder permission(String permission) {
    this.permission = permission;
    return this;
  }

  public ScopeBuilder strict() {
    this.strict = true;
    return this;
  }

  @Override
  public Scope build() {
    return new ScopeImpl(name, description, subScopes, destinations, permission, strict);
  }
}
