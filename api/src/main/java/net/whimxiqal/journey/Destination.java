package net.whimxiqal.journey;

import net.kyori.adventure.text.Component;

public interface Destination extends Describable, Permissible {

  static DestinationBuilder builder(Cell location) {
    return new DestinationBuilder(location);
  }

  static Destination of(Cell location) {
    return new DestinationImpl(Component.empty(), Component.empty(), location, null);
  }

  /**
   * The physical location of the destination.
   *
   * @return the location
   */
  Cell location();

}
