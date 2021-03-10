package edu.whimc.indicator.api.search;

public interface Locatable<T extends Locatable<T, D>, D> {

  double distanceTo(T other);

  D getDomain();

  /**
   * Get a useful identifier for this locatable for debug purposes.
   * @return an identifier string
   */
  String print();

}
