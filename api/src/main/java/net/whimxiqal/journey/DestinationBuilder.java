package net.whimxiqal.journey;

import net.kyori.adventure.text.Component;

public class DestinationBuilder implements Builder<Destination> {

  private final Cell location;
  private Component name = Component.empty();
  private Component description = Component.empty();
  private String permission = null;

  DestinationBuilder(Cell location) {
    this.location = location;
  }

  /**
   * Set the name.
   *
   * @param name the name
   * @return the builder, for chaining
   */
  public DestinationBuilder name(Component name) {
    this.name = name;
    return this;
  }

  /**
   * Set the description.
   *
   * @param description the description
   * @return the builder, for chaining
   */
  public DestinationBuilder description(Component description) {
    this.description = description;
    return this;
  }

  public DestinationBuilder permission(String permission) {
    this.permission = permission;
    return this;
  }

  @Override
  public Destination build() {
    return new DestinationImpl(name, description, location, permission);
  }
}
