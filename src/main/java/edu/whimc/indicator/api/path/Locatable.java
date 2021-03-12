package edu.whimc.indicator.api.path;

/**
 * An abstraction of a location within a Minecraft world.
 *
 * @param <T> This implementation class. This is needed for {@link #distanceTo(Locatable)}.
 * @param <D> The domain, as in a Minecraft world
 */
public interface Locatable<T extends Locatable<T, D>, D> {

  /**
   * Get the cartesian from one locatable to another, ignoring domain.
   * For comparisons between distances, use {@link #distanceToSquared(Locatable)}
   * because it is easier to compute.
   *
   * @param other the other locatable
   * @return the cartesian distance
   */
  default double distanceTo(T other) {
    return Math.sqrt(distanceToSquared(other));
  }

  /**
   * Get the square of the cartesian distance from one locatable to another.
   * This is much easier to compute than the actual distance because
   * you avoid a square root.
   *
   * @param other the other locatable
   * @return the square of the cartesian distance
   */
  double distanceToSquared(T other);

  /**
   * The domain; usually the Minecraft world;
   *
   * @return the domain of this locatable
   */
  D getDomain();

  /**
   * Get a useful identifier for this locatable for debug purposes.
   *
   * @return an identifier string
   */
  String print();

}
