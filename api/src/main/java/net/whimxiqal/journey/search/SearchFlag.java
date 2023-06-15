package net.whimxiqal.journey.search;

/**
 * An optional parameter to change the behavior of a path search.
 *
 * @param <T> the type of value associated with the flag
 */
public class SearchFlag<T> {

  private final Type type;
  private final T value;

  private SearchFlag(Type type, T value) {
    if (!type.clazz.isInstance(value)) {
      throw new IllegalArgumentException("Flag with type " + type.name()
          + " was given incompatible value type: " + value.getClass().getSimpleName());
    }
    this.type = type;
    this.value = value;
  }

  /**
   * A static constructor a {@link SearchFlag}.
   *
   * @param type  the type, to determine which behavior of the search to alter
   * @param value the value, to determine how the search behavior is altered
   * @param <T>   the type of the value
   * @return the flag
   */
  public static <T> SearchFlag<T> of(Type type, T value) {
    return new SearchFlag<>(type, value);
  }

  /**
   * The type of flag, to determine which behavior to alter.
   *
   * @return the type of flag
   */
  public Type type() {
    return type;
  }

  /**
   * The value of the flag, to determine how the behavior is altered.
   *
   * @return the value of the flag
   */
  public T value() {
    return value;
  }

  /**
   * The types of flags allowed to alter the behavior of a search.
   */
  public enum Type {

    /**
     * How long a search takes before it is stops and fails.
     */
    TIMEOUT(Integer.class),

    /**
     * Whether flight should be considered a possible mode of transportation for players
     * with the ability to fly.
     * This flag does not affect players who don't have the ability to fly anyway.
     */
    FLY(Boolean.class);

    private final Class<?> clazz;

    Type(Class<?> clazz) {
      this.clazz = clazz;
    }
  }

}
