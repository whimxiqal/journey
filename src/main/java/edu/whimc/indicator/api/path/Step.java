package edu.whimc.indicator.api.path;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A representation of a movement step between {@link Locatable}s on a {@link Trail}.
 *
 * @param <T> The locatable type
 * @param <D> The domain type
 */
@Data
@AllArgsConstructor
public final class Step<T extends Locatable<T, D>, D> {
  /**
   * An object to identify location.
   */
  private final T locatable;

  /**
   * The type of {@link Mode} that was used to get to the stored locatable.
   */
  private ModeType modeType;
}
