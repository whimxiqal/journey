package edu.whimc.indicator.api.search;

public interface Locatable<T extends Locatable<T, D>, D> {

  double distanceTo(T other);

  D getDomain();

}
