package net.whimxiqal.journey;

import net.kyori.adventure.text.Component;

public interface Describable {

  /**
   * The readable name.
   *
   * @return the name
   */
  Component name();

  /**
   * The readable description.
   *
   * @return the description
   */
  default Component description() {
    return Component.empty();
  }

}
