package net.whimxiqal.journey;

import java.util.Optional;
import net.kyori.adventure.text.Component;

class DestinationImpl implements Destination {

  private final Component name;
  private final Component description;
  private final Cell location;
  private final String permission;

  DestinationImpl(Component name, Component description, Cell location, String permission) {
    this.name = name;
    this.description = description;
    this.location = location;
    this.permission = permission;
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
  public Cell location() {
    return location;
  }

  @Override
  public Optional<String> permission() {
    return Optional.ofNullable(permission);
  }
}
