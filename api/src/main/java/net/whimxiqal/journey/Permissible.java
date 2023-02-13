package net.whimxiqal.journey;

import java.util.Optional;

public interface Permissible {

  /**
   * The permission required to use this Scope and its contents,
   * or empty if everyone is allowed.
   *
   * @return the permission
   */
  default Optional<String> permission() {
    return Optional.empty();
  }

}
