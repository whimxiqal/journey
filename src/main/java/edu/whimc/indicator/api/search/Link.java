package edu.whimc.indicator.api.search;

public final class Link<T extends Locatable<T, D>, D> {

  private final T origin;
  private final T destination;

  public Link(final T origin, final T destination) {
    this.origin = origin;
    this.destination = destination;
  }

  public T getOrigin() {
    return origin;
  }

  public T getDestination() {
    return destination;
  }
}
