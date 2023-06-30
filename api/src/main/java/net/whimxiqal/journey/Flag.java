package net.whimxiqal.journey;

public class Flag<T, V> {
  protected final T type;
  protected final V value;

  public Flag(T type, V value) {
    this.type = type;
    this.value = value;
  }

  /**
   * The type of flag, to determine which behavior to alter.
   *
   * @return the type of flag
   */
  public T type() {
    return type;
  }

  /**
   * The value of the flag, to determine how the behavior is altered.
   *
   * @return the value of the flag
   */
  public V value() {
    return value;
  }

}
